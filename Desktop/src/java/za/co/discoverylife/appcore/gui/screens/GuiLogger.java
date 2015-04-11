package za.co.discoverylife.appcore.gui.screens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import za.co.discoverylife.appcore.gui.GuiBase;
import za.co.discoverylife.appcore.gui.GuiLookAndFeel;
import za.co.discoverylife.appcore.gui.GuiMenu;
import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.logging.ILogListener;
import za.co.discoverylife.appcore.logging.LogManager;
import za.co.discoverylife.appcore.task.MetaMenu;
import za.co.discoverylife.appcore.task.MetaTask;
import za.co.discoverylife.appcore.task.TaskEntry;
import za.co.discoverylife.appcore.task.TaskManager;
import za.co.discoverylife.appcore.task.TaskNotFoundException;

/**
 * Provides a screen in which log messages are displayed. Usually added to the
 * foot of the screen. Includes setting of detail level :- All, info, report,
 * error etc. Adds a pop-up menu to the screen to control logging level.
 * 
 * @author anton11
 */
@MetaMenu(label = "_Console")
public class GuiLogger extends GuiScreen implements ILogListener, PropertyChangeListener
{
	private static final long serialVersionUID = -4661127898368162467L;

	public static final String CLEAR = "GuiLogger@logClear";
	public static final String ALL = "GuiLogger@log0";
	public static final String INFO = "GuiLogger@log3";
	public static final String REPORT = "GuiLogger@log4";
	public static final String ERROR = "GuiLogger@log6";

	private static final int MAX_ROWS = 6;

	private int displayLogLevel = LOG_INFO;
	private int displayLogRows = MAX_ROWS;

	private JScrollPane scrollPane;
	private JTextPane text;
	private Document document;
	private SimpleAttributeSet[] style = new SimpleAttributeSet[9];
	private SimpleAttributeSet styleIndentError;
	private Color bg;
	// private GuiRow row;
	GuiMenu popup;

	private boolean ishighlighted = false;

	private static int TXT_H = 0;

	/**
	 * CONSTRUCTS a log view panel with specified size.
	 * 
	 * @param width
	 * @param height
	 */
	public GuiLogger() {
		super("GuiLogger", "Logging console for GUI");
		// titledBorder("Console", 0);
		// -- register action tasks
		TaskManager.getInstance().registerModel(GuiLogger.class);
		// -- set up screen
		bg = getBackground();
		setupStyles();
		// create text component to display log in
		createTextArea();
		// create scroll pane
		createScrollPane();
		scrollPane.setViewportView(text);
		// --- status row
		newRow();
		row.add(scrollPane);

		row.padAcross();
		// route logging to this screen to be shown in the status field
		LogManager.getInstance().addLogListener(this);
		popup = new GuiMenu(this, null, "Console Logging", GuiMenu.TEXT_AND_ICON, true, "Set the level of log detail to report on");
		try {
			popup.addItem("clearHighlights");
			popup.addSeparator();
			popup.addItem("logClear");
			popup.addSeparator();
			popup.addItem("log0");
			popup.addItem("log3");
			popup.addItem("log4");
			popup.addItem("log6");
		} catch (TaskNotFoundException e) {
			e.printStackTrace();
		}
		update(LOG_ALL);
		this.addMouseListener(popup);
		text.addMouseListener(popup);
		// Dimension scrn = GuiBase.getGuiBase().getScreenSize(100, 100);
	}

	/** Responds to property changes, routing splitPane divider moves to here */
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(JSplitPane.DIVIDER_LOCATION_PROPERTY)) {
			int div = ((Integer) evt.getNewValue()).intValue();
			JSplitPane sp = (JSplitPane) evt.getSource();
			Dimension scrn = sp.getSize();
			int avail = scrn.height - div - V_GAP - V_TEXT_HEIGHT;
			scrollPane.setPreferredSize(new Dimension(scrn.width-20, avail));
			scrollPane.invalidate();
			GuiBase.getGuiBase().reshow();
		}
	}
	/**
	 * Implements ILogListener.doLog to show messages on screen
	 */
	public synchronized void doLog(int lvl, String msg, Exception e) {
		try {
			if (lvl < displayLogLevel)
				return;
			// clearHighlights();
			appendText(style[lvl], LOG_PREFIX[lvl] + msg);
			int n = 0;
			if (e != null) {
				Throwable t = e;
				while (t != null && n < 5) {
					StackTraceElement[] trcAry = t.getStackTrace();
					String ref = "";
					if (trcAry.length > 0) {
						StackTraceElement trc = trcAry[0];
						ref = trc.getClassName() + "@" + trc.getLineNumber();
					}
					appendText(styleIndentError, "    (" + ref + ") " + t.getMessage());
					t = t.getCause();
					n++;
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void highLight(String key) {
		highlight(key, Color.YELLOW);
	}

	/** Allows highlighting of keywords/phrases */
	public void highlight(String key, Color c) {
		Highlighter highlighter = text.getHighlighter();
		highlighter.removeAllHighlights();
		DefaultHighlighter.DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(c);
		int bx = 0;
		String txt = text.getText();
		if (key != null && key.length() > 0) {
			while (bx < txt.length()) {
				int ex = txt.indexOf('\n', bx);
				if (ex > 0) {
					int ix = txt.indexOf(key, bx);
					if (ix >= 0 && ix < ex) {
						try {
							highlighter.addHighlight(bx, ex, painter);
						} catch (BadLocationException e) {
						}
					}
				}
				bx = ex + 1;
			}
		}
		ishighlighted = true;
	}

	private synchronized void appendText(SimpleAttributeSet style, String msg) {
		try {
			synchronized (text) {
				int offset = document.getLength();
				document.insertString(offset, msg + "\n", style);
				text.setCaretPosition(document.getLength());
			}
		} catch (Exception e1) {
			System.err.println("Problem appending text to GuiLogger:" + e1.getMessage());
		}
	}

	@Override
	public synchronized void undo() {
		super.undo();
	}

	/**
	 * Implements ILogListener.clear to clear the screen
	 */
	public synchronized void clear() {
		try {
			synchronized (text) {
				document.remove(0, document.getLength());
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Turn off all highlights
	 */
	@MetaTask(seqId = 1, label = "Undo Highlights", hint = "Clear all highlights", icon = "page_white")
	public void clearHighlights() {
		if (ishighlighted) {
			synchronized (text) {
				text.getHighlighter().removeAllHighlights();
				ishighlighted = false;
			}
		}
	}

	@MetaTask(seqId = 10, label = "_Clear", hint = "Clear the console", icon = "page_white+#new")
	public void logClear() {
		clear();
	}

	@MetaTask(seqId = 11, label = "_All", hint = "Display all messages", icon = "file")
	public void log0() {
		update(LOG_ALL);
	}

	@MetaTask(seqId = 12, label = "_Info", hint = "Display all messages from INFO upwards (INFO,REPORT,WARN,ERROR,FATAL)", icon = "information")
	public void log3() {
		update(LOG_INFO);
	}

	@MetaTask(seqId = 13, label = "_Report", hint = "Display REPORT,WARN,ERROR and FATAL messages only", icon = "error")
	public void log4() {
		update(LOG_REPORT);
	}

	@MetaTask(seqId = 14, label = "_Error", hint = "Display ERROR and FATAL messages only", icon = "exclamation")
	public void log6() {
		update(LOG_ERROR);
	}

	public void update(int newLogLevel) {
		displayLogLevel = newLogLevel;
		// update buttons to disable currently selected level
		Class<?> k = GuiLogger.class;
		String key = TaskEntry.getBaseKey(k) + "log" + displayLogLevel;
		TaskManager tm = TaskManager.getInstance();
		List<TaskEntry> lst = tm.listAvailable(getClass());
		for (TaskEntry t : lst) {
			tm.setEnabled(t.getKey(), !t.getKey().equals(key));
			popup.propertyChange(new PropertyChangeEvent(this, t.getKey(), null, t));
		}
	}

	private void createTextArea() {
		Font logFont = GuiLookAndFeel.getPlainFont();
		text = new JTextPane();
		text.setFont(logFont);
		text.setEditable(false);
		text.setBackground(bg);
		document = text.getDocument();
		TXT_H = text.getFontMetrics(text.getFont()).getHeight();
	}

	private void createScrollPane() {
		scrollPane = new JScrollPane();
		scrollPane.setBackground(bg);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setMaxRows();
	}

	protected void setMaxRows() {
		int h = TXT_H * displayLogRows;
		scrollPane.setPreferredSize(new Dimension(0, h));
		scrollPane.invalidate();
		GuiBase.getGuiBase().reshow();
	}


	private void setupStyles() {
		style = new SimpleAttributeSet[9];
		for (int i = 0; i < 9; i++) {
			style[i] = new SimpleAttributeSet();
		}
		setStyle(LOG_DEBUG, Color.GRAY, bg, false, false);
		setStyle(LOG_INFO, Color.BLACK, bg, false, false);
		setStyle(LOG_WARN, Color.MAGENTA, bg, false, false);
		setStyle(LOG_REPORT, Color.BLUE, bg, false, false);
		setStyle(LOG_ERROR, Color.RED, bg, true, false);
		// set up indent style (error causes)
		styleIndentError = new SimpleAttributeSet();
		setStyle(styleIndentError, Color.RED, bg, false, false);
		// StyleConstants.setRightIndent(styleIndentError, 20.5f);
		// StyleConstants.setLeftIndent(styleIndentError, 2f);
	}

	private void setStyle(int logLevel, Color fg, Color bg, boolean bold, boolean italic) {
		if (logLevel < 0 || logLevel > LOG_FATAL)
			return;
		setStyle(style[logLevel], fg, bg, bold, italic);
	}

	private void setStyle(SimpleAttributeSet style, Color fg, Color bg, boolean bold, boolean italic) {
		StyleConstants.setForeground(style, fg);
		StyleConstants.setBackground(style, bg);
		StyleConstants.setBold(style, bold);
		StyleConstants.setItalic(style, italic);
		StyleConstants.setFontFamily(style, "Courier");
		StyleConstants.setFontSize(style, GuiLookAndFeel.getPlainFont().getSize());
		//System.out.println("Font size is "+GuiLookAndFeel.getPlainFont().getSize());
		//StyleConstants.setFontSize(style, 20);
	}

	public void close() {

	}

	public int getDisplayLogLevel() {
		return displayLogLevel;
	}

	public void setDisplayLogLevel(int displayLogLevel) {
		this.displayLogLevel = displayLogLevel;
	}

	public int getDisplayLogRows() {
		return displayLogRows;
	}

	public void setDisplayLogRows(int displayLogRows) {
		this.displayLogRows = displayLogRows;
		setMaxRows();
	}

}
