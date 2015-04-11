package za.co.discoverylife.appcore.gui.buttons;

import java.awt.Insets;
/**
 * Constants used for Button styles and layout.
 * 
 * @author Anton Schoultz
 */
public interface IButtonConstants
{
	public static final int BUTTON_STYLE_ICON_ONLY = 0x01;
	public static final int BUTTON_STYLE_TEXT_ONLY = 0x02;
	public static final int BUTTON_STYLE_ICON_AND_TEXT = 0x03;
	public static final int BUTTON_STYLE_MASK = 0x0F;
	
	public static final int BUTTON_LAYOUT_HORIZONTAL = 0x10;
	public static final int BUTTON_LAYOUT_VERTICAL = 0x20;
	public static final int BUTTON_LAYOUT_MASK = 0xF0;
		
	public static final int BUTTON_STYLE_DEFAULT = BUTTON_STYLE_ICON_ONLY;
	
	public static final String[] BUTTON_STYLE =
	{"Default", "icon only", "text only", "icon and text"};

	public static final Insets ZERO_INSETS =  new Insets(0, 0, 0, 0);

}
