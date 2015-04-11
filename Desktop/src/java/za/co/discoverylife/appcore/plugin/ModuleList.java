package za.co.discoverylife.appcore.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.jar.Attributes;

import za.co.discoverylife.appcore.ApplicationBase;
import za.co.discoverylife.appcore.field.ValueObject;
import za.co.discoverylife.appcore.logging.ILogger;
import za.co.discoverylife.appcore.logging.LogManager;
import za.co.discoverylife.appcore.util.FileUtil;

/** 
 * List of available plug-in modules with flag to indicate if they are active or not.
 * This list is persisted as part of the application, and is held in DataHolder. 
 * 
 * Plug-ins should be packaged into jars, with a manifest file.
 * This manifest file should have a Main-Class entry that specifies
 * the class name of the module definition class (extends IModule)
 * This class is then used to register the module.
 * 
 * @author anton11
 *
 */
public class ModuleList extends ValueObject
{

	/** List of key plug-in classes - configuration */
	protected ArrayList<ModuleEntry> modules = null;
	
	private transient ILogger log = LogManager.getLogger(getClass());
	
	private transient HashMap<String,TreeSet<String>> typeVsList = new HashMap<String, TreeSet<String>>();

	/** CONSTRUCTOR */
	public ModuleList() {
	}

	/** clears the list of entries */
	public void clear() {
		modules = new ArrayList<ModuleEntry>();
	}

	/** Adds a module entry for the provided IModule class (if not already added) */
	public void addModule(Class<? extends IModule> moduleClass) {
		checkModules();
		ModuleEntry en = new ModuleEntry(moduleClass);
		if(!modules.contains(en)){
			modules.add(en);
		}
	}

	/** Ensure that the module list is not null */
	private void checkModules() {
		if (modules == null) {
			modules = new ArrayList<ModuleEntry>();
		}
	}
	
	/** Remove the module entry corresponding to the provided module class */
	public void removeModule(Class<IModule> moduleClass){
		checkModules();
		ModuleEntry en = new ModuleEntry(moduleClass);
		int ix = modules.indexOf(en);
		if(ix>=0){
			modules.remove(ix);
		}
	}
	
	/** Invoke initialise on all active modules to register their plugins */
	public void loadActivePlugins(){
		checkModules();
		for(ModuleEntry e:modules){
			if(e.isActive()){
				try {
					e.initialise(this);
				} catch (Exception e1) {
					error("Could not initialise "+e.getShortName()+":"+e1.getMessage());
				}
			}
		}
	}
	
	/** 
	 * Register a plugin class against it's type key.
	 * This is called form the module's initialise method to register plugins.
	 * 
	 * @param key String identifying the type of plugin see {@link IPlugInConstants}
	 * @param className the name of the class in the module that provides this plugin
	 */
	public void addPlugIn(String key,String className){
		TreeSet<String> lst = typeVsList.get(key);
		if(lst==null){
			lst = new TreeSet<String>();
			typeVsList.put(key, lst);
		}
		lst.add(className);
		wr("Registered plugins "+key+" += "+className);
	}
	
	/** outputs an error message */
	private void error(String s){
		System.err.println("ModuleList:"+s);
	}
	
	/** Scans the plugins folder for new plug in jars which it adds to the available list */
	public void updateAvailableModules() {
		wr("Updating available modules...");
		ApplicationBase app = ApplicationBase.getApplicationBase();
		if(app==null) throw new NullPointerException("app is null");
		File pluginDir = app.getPluginsFolder();
		if(pluginDir==null) throw new NullPointerException("pluginDir is null");
		if(!pluginDir.exists()){
			pluginDir.mkdir();
		}
		wr("Scanning "+pluginDir.getAbsolutePath());
		for (File jar : pluginDir.listFiles()) {
			if (jar.isFile() && jar.getName().toLowerCase().endsWith(".jar")) {
				try {
					Attributes attribs = FileUtil.getManifestAttributes(jar);
					if (attribs != null) {
						String mainClass = attribs.getValue(Attributes.Name.MAIN_CLASS);
						@SuppressWarnings("unchecked")
						Class<IModule> mc = (Class<IModule>) Class.forName(mainClass);
						addModule(mc);
						wr("PlugInManager:Found plugin "+mainClass+" in "+jar.getName());
					}
				} catch (Exception e) {
					error("Problem with registering plugins in " + jar.getName()+" ** "+e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	/** 
	 * Returns a list of class names that provide the specified type of plugin.
	 * This is used to actually plug in the functionality by the likes of
	 * GuiBase (to plug in main menus) etc.
	 *
	 * @param key Type of plugin to return
	 * @return
	 */
	public TreeSet<String> getClassNames(String key){
		checkModules();
		TreeSet<String> list = new TreeSet<String>();
		for(ModuleEntry e:modules){
			if(e.isActive()){
				try {
					TreeSet<String> sublist = typeVsList.get(key);
					if(sublist!=null){
						list.addAll(sublist);
					}
				} catch (Exception ex) {
					log.error("Problem getting tools menues for "+e.getShortName(),ex);
				}
			}
		}
		return list;
	}

	/** Returns a list of all modules - used for plugin manager to enable/disable */
	public ArrayList<ModuleEntry> getModules() {
		return modules;
	}
	

}
