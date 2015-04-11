package za.co.discoverylife.appcore.plugin;

/** Defines a PlugIn module */
public interface IModule
{
	/** Called by framework to allow registration of all plugIns in the module */
	public void initialise(ModuleList manager) throws Exception;
}
