package za.co.discoverylife.appcore.gui;

import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * Extends JFrame in order to trap and over-ride the window close event
 * by routing it to GuiBAse.exit() instead.
 * 
 * @author anton11
 *
 */
public class GuiFrame extends JFrame
{
	private static final long serialVersionUID = -6496887207369663485L;

  /**
   * Processes window events occurring on this component.
   * over-ride window closing to perform File->Exit.
   *
   * @param  e  the window event
   */
  protected void processWindowEvent(WindowEvent e) {
      if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      	// over-ride window closing to perform File->Exit
      	GuiBase.getGuiBase().exit();
      }else{
      	// not a window close so let parent class handle it
        super.processWindowEvent(e);
      }
  }

}
