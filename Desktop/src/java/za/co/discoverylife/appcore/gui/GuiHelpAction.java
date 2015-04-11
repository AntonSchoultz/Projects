package za.co.discoverylife.appcore.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import za.co.discoverylife.appcore.gui.controller.GuiController;

/**
 * This action looks up the currently selected panel and then displays
 * it's associated HTML help page as a dialogue screen.
 * It is referenced in GuiBase.createAndShowGUI() where it is bound to the F12 key.
 * 
 * @author anton11
 */
public class GuiHelpAction
		extends AbstractAction
{
	private static final long serialVersionUID = 7065349556488442389L;

	/** 
	 * Handles help request (F12) for a panel by trying to find 
	 * an HTML help page with the same name as the panel and then displaying it.
	 */
	public void actionPerformed(ActionEvent e) {
		GuiController ctrlr = GuiBase.getGuiBase().getGuiController();
		String name = ctrlr.getActivePanel().getName();
		if(name.indexOf(".")>0){
			return;// no help for displayed files (which have extension)
		}
		int ix = name.indexOf(':');
		if (ix > 0) {
			name = name.substring(0, ix);
		}
		String page = "docs/" + name + ".htm";
		GuiBase.getGuiBase().displayTextResourceAsDialogue(page, "Help for '" + name + "' screen.");
		//GuiBase.getGuiBase().displayTextResourceAsPanel(page, "Help for '" + name + "' screen.");
	}

}
