package za.co.discoverylife.appcore.gui.buttons;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import za.co.discoverylife.appcore.gui.GuiIconManager;
import za.co.discoverylife.appcore.gui.GuiPanel;
import za.co.discoverylife.appcore.task.MetaTask;
import za.co.discoverylife.appcore.task.TaskEntry;
import za.co.discoverylife.appcore.task.TaskManager;
import za.co.discoverylife.appcore.task.TaskNotFoundException;

/**
 * Custom Button with text/icon and actionKey.
 * 
 * @author anton11
 */
public class GuiButton
		extends JButton implements IButtonConstants 
{
	public Object getDataObject() {
		return dataObject;
	}

	public void setDataObject(Object dataObject) {
		this.dataObject = dataObject;
	}

	private static final long serialVersionUID = 2151923185837750240L;
	private static int buttonStyle = BUTTON_STYLE_DEFAULT;
	protected static TaskManager taskManager = TaskManager.getInstance();

	protected Icon myIcon;
	protected String myText;
	protected String value;
	
	protected Object dataObject;
	
	/** CONSTRUCT a button with the provided text and icon */
	public GuiButton(String text, Icon icon) {
		super(text, icon);
		setMargin(ZERO_INSETS);
		this.myIcon = icon;
		if (text != null && text.trim().length() > 0) {
			this.myText = " " + text + " ";
		} else {
			this.myText = null;
		}
		undo();
	}

	/** CONSTRUCT a button with the provided text and named icon */
	public GuiButton(String text, String iconRef) {
		this(text, GuiIconManager.getIcon(iconRef));
	}

	/** Sets the button style for this panel 0=default */
	public static void setButtonStyle(int n) {
		if (n == 0) {
			buttonStyle = BUTTON_STYLE_DEFAULT;
		} else {
			buttonStyle = n;
		}
	}
	
	/** sets the icon based on the reference passed in */
	public void loadIcon(String iconRef){
		 myIcon = GuiIconManager.getIcon(iconRef);
		 super.setIcon(myIcon);
	}

	/**
	 * Creates a button for the actionKey given
	 * 
	 * @param actionKey
	 * @return
	 * @throws TaskNotFoundException
	 */
	public static GuiButton create(String actionKey)
			throws TaskNotFoundException {
		TaskEntry ae = taskManager.findEntry(actionKey);
		MetaTask ma = ae.getMeta();
		ImageIcon icon = null;
		if (ma.icon().length() > 0) {
			icon = GuiIconManager.getIcon(ma.icon());
		}
		GuiButton btn = new GuiButton(ma.label(), icon);
		btn.setToolTipText(ma.hint());
		btn.setActionCommand(actionKey);
		btn.undo();
		return btn;
	}

	/**
	 * Update the button's state - ICON and/or TEXT
	 */
	public void undo() {
		// set enabled / disabled based on task manager state
		String key = getActionCommand();
		setEnabled(taskManager.isEnabled(key));
		// display icon and/or text
		if ((getButtonStyle() & BUTTON_STYLE_MASK) == 0) {
			setButtonStyle(BUTTON_STYLE_DEFAULT);
		}
		int style = getButtonStyle();
		setDisplayStyle(style);
	}
	
	/** sets horizontal alignment to the left */
	public void justifyLeft(){
		setHorizontalAlignment(SwingConstants.LEFT);
	}
	
	/** sets horizontal alignment to the right */
	public void justifyRight(){
		setHorizontalAlignment(SwingConstants.RIGHT);
	}

	/** Make the button small - no border space */
	public GuiButton setAsMiniButton() {
		setAsMiniButton(4);
		return this;
	}
	
	/** Make the button small - no border space */
	public GuiButton setAsMiniButton(int ratio) {
		setBorderPainted(true);
		int tb = 1;
		int lr = 1;
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), BorderFactory
						.createEmptyBorder(tb, lr, tb, lr)));
		setDisplayStyle(BUTTON_STYLE_ICON_ONLY);
		// GuiPanel.adjustSizeFreeForm(this);
		GuiPanel.adjustWidth(this, GuiPanel.W_ICON * ratio);
		return this;
	}

	/** Set if button should display as icon only/text only/ icon and text */
	public void setDisplayStyle(int style) {
		switch ((style & BUTTON_STYLE_MASK)) {
			case BUTTON_STYLE_ICON_ONLY :
				if (myIcon != null) {
					useIcon();
				} else {
					useText();
				}
				break;
			case BUTTON_STYLE_TEXT_ONLY :
				if (myText != null) {
					useText();
				} else {
					useIcon();
				}
				break;
			case BUTTON_STYLE_ICON_AND_TEXT :
				if (myIcon == null) {
					useText();
				} else if (myText == null) {
					useIcon();
				} else {
					setText(myText);
					setIcon(myIcon);
					GuiPanel.adjustSizeFreeForm(this);
				}
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
				break;
		}
		repaint();
	}

	/** Activates the text part of the button */
	private void useText() {
		setText(myText);
		setIcon(null);
		GuiPanel.adjustSizeFreeForm(this);
	}

	/** Activates the icon part of the button */
	private void useIcon() {
		setText(null);
		setIcon(myIcon);
		GuiPanel.adjustWidth(this, GuiPanel.W_ICON);
	}

	/** return the current button style mode */
	public static int getButtonStyle() {
		return buttonStyle;
	}

	/** Returns the data value */
	public String getValue() {
		return value;
	}

	/** Sets the data value */
	public void setValue(String value) {
		this.value = value;
	}

}
