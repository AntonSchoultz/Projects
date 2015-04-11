package za.co.discoverylife.appcore.plugin;

import za.co.discoverylife.appcore.field.MetaFieldInfo;

/**
 * Registration entry of module class name and boolean active flag.
 * Has methods for comparing (sorting), obtaining descriptions etc.
 * 
 * @author Anton Schoultz 
 */
public class ModuleEntry implements Comparable<ModuleEntry>
{
	// persisted fields
	private String moduleClassName = null;
	
	@MetaFieldInfo(label = "Active", hint = "Turn plug-in module on/off (May require a restart)")
	private boolean active = true;

	// derived convenience fields
	private transient Class<? extends IModule> moduleClass=null;
	private transient MetaPlugIn meta=null;
	private transient IModule module;
	
	/** CONSTRUCTOR - default */
	public ModuleEntry(){
		super();
	}

	/** CONSTRUCTOR which accepts the plug-in class name */
	public ModuleEntry(Class<? extends IModule> moduleClass2) {
		super();
		this.moduleClass = moduleClass2;
		this.moduleClassName = moduleClass2.getName();
		meta = (MetaPlugIn) moduleClass2.getAnnotation(MetaPlugIn.class);
	}
	
	@SuppressWarnings("unchecked")
	/** Obtains Class, instantiates module and gets meta information */
	public void prep() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		if(moduleClass==null){
			moduleClass = (Class<IModule>) Class.forName(moduleClassName);
			module = (IModule) moduleClass.newInstance();
			meta = (MetaPlugIn) moduleClass.getAnnotation(MetaPlugIn.class);
		}
	}
	
	/** Initialises the module which lets the module register all of it's plugin functions */
	public void initialise(ModuleList modList) throws Exception{
		prep();
		module.initialise(modList);
	}
	
	/** Returns a short name for the module */
	public String getShortName(){
		if(moduleClass==null){
			int ix = moduleClassName.lastIndexOf(".");
			return moduleClassName.substring(ix+1);
		}
		return moduleClass.getSimpleName();
	}

	/** Returns the module's description */
	public String getDescription(){
		return meta==null ? moduleClassName : meta.description();
	}

	/** Returns the module's help file name */
	public String getHelpName(){
		return meta==null ? "" : meta.helpName();
	}

	/** Returns true if the module is flagged as active */
	public boolean isActive() {
		return active;
	}

	/** Sets the module as active/inactive */
	public void setActive(boolean active) {
		this.active = active;
	}

	/** Compares module entries just by the module class names */
	public int compareTo(ModuleEntry o) {
		return moduleClassName.compareTo(o.moduleClassName);
	}

	@Override
	/** Tests if module entries are the same (by class name only) */
	public boolean equals(Object obj) {
		if(obj==null) return false;
		if(!( obj instanceof ModuleEntry) ) return false;
		return compareTo( (ModuleEntry)obj) == 0;
	}
	
}
