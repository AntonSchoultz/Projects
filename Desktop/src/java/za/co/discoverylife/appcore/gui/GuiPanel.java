package za.co.discoverylife.appcore.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.lang.reflect.Method;
import java.security.InvalidParameterException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import za.co.discoverylife.appcore.field.IEditable;
import za.co.discoverylife.appcore.gui.buttons.GuiButton;
import za.co.discoverylife.appcore.logging.ILogger;
import za.co.discoverylife.appcore.logging.LogManager;
import za.co.discoverylife.appcore.task.ITaskConstants;
import za.co.discoverylife.appcore.task.TaskManager;

/**
 * Main container for all screens/panels
 * 
 * @author anton11
 */
public class GuiPanel
		extends JPanel
		implements ITaskConstants, IEditable, Runnable, Comparable<GuiPanel>
{
	private static final long serialVersionUID = -1515605715904517543L;
	/** Width for text fields */
	public static int W_TEXT_CHARS = 35;
	/** Gap between rows */
	public static int V_GAP = 2;
	/** Gap between items on a row */
	public static int H_GAP = 2;
	/** Height of text (read from font metrics) */
	public static int V_TEXT_HEIGHT;
	/** Width of a character */
	public static int W_CHAR;
	/** Field label width */
	public static int W_LABEL;
	/** Width for numeric fields */
	public static int W_NUMBER;
	public static int W_TEXT;
	/** Width for check boxes fields */
	public static int W_CHECK_BOX;
	/** Width for icons fields */
	public static int W_ICON;
	public static int W_LABEL_TEXT;

	protected transient GuiPanel guiparent;
	protected transient GuiDialog dialog = null;
	protected transient ILogger log;
	protected TaskManager taskManager = TaskManager.getInstance();
	protected boolean closeable = true;


	public GuiButton btnFired;

	public GuiButton getBtnFired() {
		return btnFired;
	}

	public void setBtnFired(GuiButton btnFired) {
		this.btnFired = btnFired;
	}

	/**
	 * CONSTRUCTS a basic screen.
	 */
	public GuiPanel() {
		super();
		log = LogManager.getLogger(this.getClass());
	}

	/**
	 * Queues an AWT event to call undo() which will update the screen
	 */
	public void update(){
		TaskManager.getInstance().doTask(this, TaskManager.TASK_AWT_EVENT);
	}

	// --- Implement : Comparable<GuiPanel>

	/** Returns comparison if this panel has the same name as the compared panel */
	public int compareTo(GuiPanel o) {
		return getName().compareTo(o.getName());
	}

	// --- Implement : Runnable

	/** Allows queuing of undo() operation to update the screen after model tasks */
	public void run() {
		undo();
	}

	// --- Implement : IEditable

	/** Commit the edit value to the object */
	public void commit() {
		for (Component comp : getComponents()) {
			if (comp instanceof IEditable) {
				((IEditable) comp).commit();
			}
		}
	}

	/** Set edit value from the object field value */
	public void undo() {
		for (Component comp : getComponents()) {
			if (comp instanceof IEditable) {
				((IEditable) comp).undo();
			}
		}
	}

	/**
	 * set the edited value to the initial value (if provided)
	 */
	public void reset() {
		for (Component comp : getComponents()) {
			if (comp instanceof IEditable) {
				((IEditable) comp).reset();
			}
		}
	}

	// ----

	/** Erase/remove this panel and it's children from the parent panel */
	public void erase() {
		setVisible(false);
		// first remove the children
		for (Component kid : getComponents()) {
			// if child is a GuiComponent call erase on it first
			if (kid instanceof GuiPanel) {
				((GuiPanel) kid).erase();
			}
			remove(kid);
		}
		// remove self from parent
		if (this.guiparent != null) {
			guiparent.remove(this);
		}
	}

	// ----

	/** Remove this panel from the display */
	public void closeScreen() {
		GuiBase.getGuiBase().getGuiController().removePanel(getName());
	}

	/** Sets field widths in terms of character width provided */
	public static void initWidths(FontMetrics fm) {
		fm.getFont();
		int[] wa = fm.getWidths();
		int wc = 0;
		for (int n : wa) {
			if (n > wc)
				wc = n;
		}
		V_TEXT_HEIGHT = fm.getMaxAscent() + fm.getMaxDescent() + fm.getLeading() + 2;
		// pointSize * 4 / 3;
		
		W_CHAR = wc;// pointSize;
		//V_GAP = V_TEXT_HEIGHT / 4;
		//H_GAP = W_CHAR;
		W_CHECK_BOX = W_CHAR * 2;
		W_LABEL = 15 * W_CHAR;
		W_NUMBER = 10 * W_CHAR;
		W_TEXT = W_TEXT_CHARS * W_CHAR;
		W_ICON = 32;// 3 * W_CHAR;
		W_LABEL_TEXT = W_LABEL + H_GAP + W_TEXT;
	}

	/** Log an error message with it's exception/stack trace */
	protected void error(String msg, Exception e) {
		log.error(msg + " in " + this.getClass().getSimpleName(), e);
	}

	/** Apply titled border to whole screen with thickness gap */
	public void titledBorder(String title, int thickness) {
		if (title != null) {
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createTitledBorder(title), BorderFactory
							.createEmptyBorder(thickness, thickness, thickness, thickness)));
		} else {
			setBorder(BorderFactory.createRaisedBevelBorder());
		}
	}

	/** Sets the parent panel */
	public void setParentGui(GuiPanel parent) {
		this.guiparent = parent;
	}

	/** return the top-level root panel */
	public GuiPanel getRootPanel() {
		GuiPanel p = this;
		while (p.guiparent != null) {
			p = p.guiparent;
		}
		return p;
	}

	/**
	 * Sets the colours for this component and it's children
	 * 
	 * setColors
	 * 
	 * @param fg
	 *          Foreground colour (or null for default)
	 * @param bg
	 *          Background colour (or null for default)
	 */
	public void setColors(Color fg, Color bg) {
		for (Component kid : getComponents()) {
			if (kid instanceof GuiPanel) {
				((GuiPanel) kid).setColors(fg, bg);
			} else {
				kid.setForeground(fg);
				kid.setBackground(bg);
			}
		}
		setForeground(fg);
		setBackground(bg);
	}

	/**
	 * Navigate up the GuiPanel tree to the first GuiScreen
	 * 
	 * @return
	 */
	public GuiScreen getScreen() {
		GuiPanel p = this;
		while (p != null) {
			if (p instanceof GuiScreen) {
				return (GuiScreen) p;
			}
			p = p.guiparent;
		}
		return null;
	}

	/** used to fire a named void null method in the screen */
	@SuppressWarnings("rawtypes")
	public void fireMethod(String methodName, boolean failIfMissing) {
		Class[] ca = new Class[0];
		Object[] oa = new Object[0];
		try {
			Method method = this.getClass().getMethod(methodName, ca);
			method.invoke(this, oa);
		} catch (Exception e) {
			if (failIfMissing) {
				throw new InvalidParameterException("Method '" + methodName + "' not found in " + this.getClass().getName());
			}
		}
	}

	/** used to fire a named void null method in the screen */
	@SuppressWarnings("rawtypes")
	public void fireMethod(String methodName, Object data, boolean failIfMissing) {
		Class[] ca = new Class[1];
		ca[0] = data.getClass();
		Object[] oa = new Object[1];
		oa[0] = data;
		try {
			Method method = this.getClass().getMethod(methodName, ca);
			method.invoke(this, oa);
		} catch (Exception e) {
			if (failIfMissing) {
				throw new InvalidParameterException("Method '" + methodName + "' not found in " + this.getClass().getName());
			}
		}
	}

	/** Reset size to default behaviour */
	public static void adjustSizeFreeForm(JComponent comp) {
		comp.setPreferredSize(null);
		comp.setMaximumSize(null);
		comp.setMinimumSize(null);
	}

	/** Adjust the size of the component */
	public static void adjustSize(JComponent comp, Dimension dim) {
		comp.setMaximumSize(dim);
		comp.setMinimumSize(dim);
		comp.setPreferredSize(dim);
	}

	/** Adjust the size of the component */
	public static void adjustSize(JComponent comp, int w, int h) {
		Dimension dimMax = comp.getMaximumSize();
		if (w == 0) {
			w = dimMax.width;
		}
		if (h == 0) {
			h = dimMax.height;
		}
		adjustSize(comp, new Dimension(w, h));
	}

	/** Adjust the width of the component */
	public static void adjustWidth(JComponent comp, int w) {
		int h = comp.getMinimumSize().height;
		adjustSize(comp, new Dimension(w, h));
	}

	/** Adjust the height of the component */
	public static void adjustHeight(JComponent comp, int h) {
		int w = comp.getMinimumSize().width;
		adjustSize(comp, new Dimension(w, h));
	}

	/** Returns true if this panel may be closed from main screen */
	public boolean isCloseable() {
		return closeable;
	}

	/** Sets whether this panel may be closed or not */
	public void setCloseable(boolean closeable) {
		this.closeable = closeable;
	}

	protected void wr(String s) {
		System.out.println(this.getClass().getName() + ">" + s);
	}

	public GuiDialog getDialog() {
		return dialog;
	}

	public void setDialog(GuiDialog dialog) {
		this.dialog = dialog;
	}

}
