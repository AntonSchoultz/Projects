package za.co.discoverylife.appcore.plugin;

import za.co.discoverylife.appcore.DataHolder;
import za.co.discoverylife.appcore.gui.GuiBase;
import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.gui.buttons.GuiButtonPanel;
import za.co.discoverylife.appcore.task.MetaTask;

/**
 * Allows the user to turn plug-ins on/off
 * 
 * @author anton11
 *
 */
public class GuiPlugInManager extends GuiScreen
{
	public static final String PANEL_NAME = "PlugIn";

	private static final long serialVersionUID = 1353810527277852422L;

	private static ModuleList modlist = (ModuleList) DataHolder.recall(ModuleList.class);
		
	public GuiPlugInManager() {
		super(PANEL_NAME,"Manage active plug-ins");
		for(ModuleEntry entry: modlist.getModules()){
			addRow( new GuiPlugInEntryRow(entry), H_GAP);
			row.padAcross();
		}
		try {
			GuiButtonPanel br = addButtonPanel(this);
			br.padAcross(H_GAP);
			br.addAllButtons();
			br.padAcross();
		} catch (Exception e) {
			log.error("Problem adding buttons", e);
		}
		endPage();
	}

	public GuiPlugInManager(String name, String toolTip) {
		this();
	}

	@MetaTask(seqId = 90, label = "OK", hint = "Commit changes", icon = "accept")
	public void ok() {
		commit();
		GuiBase.getGuiBase().getGuiController().removePanel(getName());
		GuiBase.undo();
	}

	
}
