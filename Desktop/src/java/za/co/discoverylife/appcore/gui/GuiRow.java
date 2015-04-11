package za.co.discoverylife.appcore.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import za.co.discoverylife.appcore.field.FieldAccessException;
import za.co.discoverylife.appcore.field.FieldAccessor;
import za.co.discoverylife.appcore.field.IEditable;
import za.co.discoverylife.appcore.gui.buttons.GuiButton;
import za.co.discoverylife.appcore.gui.buttons.GuiButtonPanel;
import za.co.discoverylife.appcore.gui.buttons.GuiToggleButton;

/**
 * Row on a screen
 * 
 * @author anton11
 */
public class GuiRow extends GuiPanel
{
	private static final long serialVersionUID = 3809578863818903252L;
	private List<IEditable> fields = new ArrayList<IEditable>();

	/**
	 * CONSTRUCTS a basic screen row with horizontal layout.
	 */
	public GuiRow() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		GuiScreen scrn = getScreen();
		if (scrn != null) {
			if (scrn.log != null) {
				log = scrn.log;
			}
		}
	}

	/**
	 * Commit changes from screen to data object
	 */
	@Override
	public void commit() {
		super.commit();
		for (IEditable gf : fields) {
			gf.commit();
		}
	}

	/**
	 * Update GUI from data object
	 */
	@Override
	public void undo() {
		super.undo();
		for (IEditable gf : fields) {
			gf.undo();
		}
	}

	/**
	 * Set GUI fields to initial values
	 */
	@Override
	public void reset() {
		super.reset();
		for (IEditable gf : fields) {
			gf.reset();
		}
	}

	/** Apply horizontal glue */
	public void padAcross() {
		add(Box.createHorizontalGlue());
	}

	/** set horizontal position for next component */
	public void tabPosition(int pos) {
		int w = this.getMaximumSize().width;
		if (pos > w) {
			padAcross(pos - w - H_GAP);
		}
	}

	/** set horizontal position for next component as a % of width */
	public void tabPercent(int pct) {
		int pos = GuiBase.getGuiBase().width * (pct % 100) / 100;
		tabPosition(pos);
	}

	/** Apply horizontal gap */
	public void padAcross(int hGap) {
		add(Box.createHorizontalStrut(hGap));
	}

	/** Adds a cell to the row, preceded by H_GAP */
	public void addCell(JComponent comp) {
		add(comp);
		if (comp instanceof GuiPanel) {
			((GuiPanel) comp).setColors(getForeground(), getBackground());
			((GuiPanel) comp).setParentGui(this);
			((GuiPanel) comp).undo();
		}
		padAcross(H_GAP);
	}

	/** Add the specified label and help-text (Right justify the label */
	public JLabel addLabelRight(String text, String tooltip) {
		return addLabelRight(text, tooltip, W_LABEL);
	}
	
	/** Add the specified label and help-text (Right justify the label */
	public JLabel addLabelRight(String text, String tooltip,int width) {
		JLabel lbl = new JLabel(text);
		lbl.setToolTipText(tooltip);
		lbl.setHorizontalAlignment(SwingConstants.RIGHT);
		if(width!=0){
			adjustWidth(lbl,width);
		}
		addCell(lbl);
		return lbl;
	}

	/** Add the specified label and help-text */
	public JLabel addLabel(String text, String tooltip) {
		return addLabel(text, tooltip, W_LABEL);
	}

	/** Add the specified label and help-text */
	public JLabel addLabel(String text, String tooltip, int width) {
		JLabel lbl = new JLabel(text);
		lbl.setToolTipText(tooltip);
		if (width > 0) {
			adjustWidth(lbl, width);
		}
		addCell(lbl);
		return lbl;
	}

	/** Adds a right justified field name, with tooltip and string value */
	public JLabel addReadOnly(String text, String tooltip, String value) {
		JLabel lbl = new JLabel("<html><b>" + text + "</b>");
		lbl.setToolTipText(tooltip);
		lbl.setHorizontalAlignment(SwingConstants.RIGHT);
		adjustWidth(lbl, W_LABEL);
		addCell(lbl);
		JLabel lblValue = new JLabel(value);
		lblValue.setToolTipText(tooltip);
		adjustWidth(lblValue, W_LABEL);
		addCell(lblValue);
		padAcross();
		return lbl;
	}

	/** Add a GUI edit field for the specified data object and field name */
	public GuiField addField(Object object, String fieldName) {
		try {
			GuiField gf = new GuiField(this, object, fieldName);
			fields.add(gf);
			addCell(gf.comp);
			return gf;
		} catch (FieldAccessException e) {
			error("Problem adding field " + fieldName, e);
			return null;
		}
	}
	
	/** Add all fields of object to this row */
	public void addFields(Object object){
		List<FieldAccessor> fl = FieldAccessor.listAccessorsForObject(object);
		for(FieldAccessor fa:fl){
			addField(object,fa.getFieldName());
		}
	}
	
	/** Add all fields of object to this row */
	public void addHeadings(Object object){
		List<FieldAccessor> fl = FieldAccessor.listAccessorsForObject(object);
		for(FieldAccessor fa:fl){
			addLabel(fa.getLabel(), fa.getHint(),fa.getSize()*W_CHAR);
		}
	}

	/** Add a GUI edit field for the specified data object and field name */
	public GuiToggleButton addToggleButton(Object object, String fieldName) {
		GuiToggleButton gf = new GuiToggleButton(object, fieldName);
		fields.add(gf);
		addCell(gf);
		return gf;
	}

	/**
	 * Add a Label and a GUI edit field for the specified data object and field
	 * name
	 */
	public GuiField addLabelAndField(Object object, String fieldName) {
		try {
			GuiField gf = new GuiField(this, object, fieldName);
			addGuiField(gf);
			if (!gf.isSubScreen()) {
				JLabel lbl = addLabel(gf.getLabel(), gf.getToolTip());
				lbl.setHorizontalAlignment(SwingConstants.RIGHT);
			}
			addCell(gf.comp);
			if (gf.isSubScreen()) {
				padAcross();
			}
			return gf;
		} catch (FieldAccessException e) {
			error("Problem adding label and field to the screen for '" + fieldName + "'", e);
			return null;
		}
	}

	/** Adds GuiField to list for undo/commit etc */
	public void addGuiField(GuiField gf) {
		fields.add(gf);
	}

	/**
	 * Add a row with Label and a GUI edit field for the specified data object and
	 * field name
	 */
	public GuiField addLabelAndFieldRow(Object object, String fieldName) {
		GuiField gf = addLabelAndField(object, fieldName);
		padAcross();
		return gf;
	}

	/** Add a button which will action on the provided model */
	public GuiButton addButton(Object model, String actionKey) {
		try {
			GuiButtonPanel btns = addButtonPanel(model);
			GuiButton btn = btns.addButton(actionKey);
			return btn;
		} catch (Exception e) {
			error("Problem adding button for actionkey=" + actionKey, e);
		}
		return null;
	}

	/**
	 * Add a button which will action on the screen
	 * 
	 * @return
	 */
	public GuiButton addButton(String actionKey) {
		GuiButton btn = null;
		try {
			GuiButtonPanel bp = new GuiButtonPanel(getScreen());
			bp.setParentGui(getScreen());
			btn = bp.addButton(actionKey);
			btn.setBorder(null);

			addCell(bp);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return btn;
	}

	/** Adds a panel to hold buttons for a given model */
	public GuiButtonPanel addButtonPanel(Object model) {
		try {
			GuiButtonPanel btnPanel = new GuiButtonPanel(model);
			btnPanel.setParentGui(this);
			addCell(btnPanel);
			return btnPanel;
		} catch (Exception e) {
			error("Problem adding button panel", e);
			return null;
		}
	}

}
