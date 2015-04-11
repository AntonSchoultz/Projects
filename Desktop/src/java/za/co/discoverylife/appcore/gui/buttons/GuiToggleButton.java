package za.co.discoverylife.appcore.gui.buttons;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import za.co.discoverylife.appcore.field.FieldAccessException;
import za.co.discoverylife.appcore.field.FieldAccessor;
import za.co.discoverylife.appcore.field.IEditable;
import za.co.discoverylife.appcore.gui.GuiIconManager;
import za.co.discoverylife.appcore.gui.GuiPanel;
import za.co.discoverylife.appcore.task.TaskManager;

/**
 * Custom Button with text/icon and actionKey.
 * 
 * @author anton11
 */
public class GuiToggleButton
		extends JToggleButton
		implements IEditable, ActionListener, IButtonConstants {
	private static final long serialVersionUID = 2151923185837750240L;


	protected static TaskManager taskManager = TaskManager.getInstance();

	protected Icon myIcon;
	protected String myText;
	protected String hint;

	private FieldAccessor fieldAccess;

	// dimensions for button
	private int minW = GuiPanel.W_ICON;
	private int minH = GuiPanel.V_TEXT_HEIGHT+4;
	private int maxW = GuiPanel.W_LABEL/2;
	private int maxH = GuiPanel.V_TEXT_HEIGHT*2+8;
	

	/**
	 * Constructs a toggle button to set a boolean field to true/false.
	 * 
	 * @param dataObject
	 * @param fieldName
	 * @throws FieldAccessException
	 */
	public GuiToggleButton(Object dataObject, String fieldName) {
		super("TglBtn");
		setMargin(ZERO_INSETS);

		try {
			fieldAccess = new FieldAccessor(dataObject, fieldName);
		} catch (FieldAccessException e) {
			throw new RuntimeException("GuiToggleButton data object is null or field name is invalid", e);
		}
		if (fieldAccess.getFieldType() != FieldAccessor.TYPE_BOOLEAN) {
			throw new RuntimeException("GuiToggleButton may only be used for boolean fields");
		}
		String IconName = fieldAccess.getIconName();
		myText = fieldAccess.getLabel();
		hint = fieldAccess.getHint();
		setText(myText);
		if (IconName != null) {
			myIcon = GuiIconManager.getIcon(IconName);
			setIcon(myIcon);
		}
		addActionListener(this);
		setAsMiniButton();
		undo();
	}

	/** Button clicked */
	public void actionPerformed(ActionEvent e) {
		commit();
	}

	/**
	 * Update the button's state - ICON and/or TEXT
	 */
	public void undo() {
		setDisplayStyle(GuiButton.getButtonStyle());
		// set on/off
		boolean flag = (Boolean) fieldAccess.getFieldObject();
		setSelected(flag);
		setToolTipText(hint + " : " + flag);
	}

	public void commit() {
		boolean flag = isSelected();
		fieldAccess.setFieldObject(flag);
		setToolTipText(hint + " : " + flag);
	}

	public void reset() {
		boolean flag = (Boolean) fieldAccess.getFieldObject();
		setSelected(flag);
		setToolTipText(hint + " : " + flag);
	}

	/** Make the button small - no border space */
	public void setAsMiniButton() {
		setBorderPainted(true);
		int tb = 1;
		int lr = 1;
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), BorderFactory
						.createEmptyBorder(tb, lr, tb, lr)));
		setDisplayStyle(BUTTON_STYLE_ICON_ONLY);
		GuiPanel.adjustSizeFreeForm(this);
		//GuiPanel.adjustWidth(this, GuiPanel.W_ICON * 4);
	}
	

	/** Set if button should display as icon only/text only/ icon and text */
	public void setDisplayStyle(int style) {
		int w=minW;
		int h=minH;
		boolean both=false;
		switch ((style & BUTTON_STYLE_MASK)) {
			case BUTTON_STYLE_ICON_ONLY :
				setText(null);
				setIcon(myIcon);
				w = minW;
				//GuiPanel.adjustWidth(this, GuiPanel.W_ICON);
				break;
			case BUTTON_STYLE_TEXT_ONLY :
				setText(myText);
				setIcon(null);
				w = maxW;
				//GuiPanel.adjustWidth(this, GuiPanel.W_LABEL);
				//GuiPanel.adjustSizeFreeForm(this);
				break;
			case BUTTON_STYLE_ICON_AND_TEXT :
				setText(myText);
				setIcon(myIcon);
				w = maxW;
				both=true;
				// GuiPanel.adjustWidth(this, GuiPanel.W_LABEL);
				//GuiPanel.adjustSizeFreeForm(this);
				break;
		}
		switch( (style & BUTTON_LAYOUT_MASK) ){
			default:
			case BUTTON_LAYOUT_HORIZONTAL:
				setVerticalTextPosition(JButton.CENTER);
				setHorizontalTextPosition(JButton.RIGHT);
				break;
			case BUTTON_LAYOUT_VERTICAL:
				setVerticalTextPosition(JButton.BOTTOM);
				setHorizontalTextPosition(JButton.CENTER);
				if(both)h=maxH;
				break;
		}
		GuiPanel.adjustSize(this, w, h);
		repaint();
	}

}
