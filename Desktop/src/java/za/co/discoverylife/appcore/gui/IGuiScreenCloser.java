package za.co.discoverylife.appcore.gui;

/** 
 * Specifies an object which can close a GuiScreen 
 * 
 * Used in GuiDialog to pick up value when dialogue is closed.
 * 
 * @author Anton Schoultz 
 */
public interface IGuiScreenCloser {
	/** Called to close a screen */
	public void closeGuiScreen(GuiScreen screen);
}
