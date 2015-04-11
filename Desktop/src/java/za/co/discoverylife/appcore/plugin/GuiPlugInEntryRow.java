package za.co.discoverylife.appcore.plugin;

import za.co.discoverylife.appcore.gui.GuiRow;

/**
 * GUI component for editing plug-in (in)active etc.
 * 
 * @author Anton Schoultz
 */
public class GuiPlugInEntryRow extends GuiRow
{
	private static final long serialVersionUID = -5856303157766592350L;
	
	/** CONSTRUCTOR which accepts a PlugInEntry object */
	public GuiPlugInEntryRow(ModuleEntry entry){
		String name = entry.getShortName();
		String desc = entry.getDescription();
		String hint = entry.getClass().getName();
		addLabelRight(name, hint,W_LABEL);
		addField(entry,"active");
		addLabel(desc,hint,W_LABEL_TEXT);
	}

}
