package za.co.discoverylife.appcore.task;

import java.util.HashMap;
import java.util.Properties;

import za.co.discoverylife.appcore.logging.ILogger;
import za.co.discoverylife.appcore.logging.LogManager;

/**
 * This task is invoked when the HTML viewer actions a hyper link which
 * contains the special text '[Action:'
 * It expects the link to have the form
 * <br> ...[Action:actionName
 * <br> ...[Action:actionName?var1=value {&var2=value2}...
 * 
 * It looks up an action class '${actionName}Action', and if found
 * calls the doAction(~) method on a new instance of that class
 * 
 * @author Anton Schoultz
 *
 */
public class LinkTask extends BaseTask
{
	public static final String ACTION = "[Action:";

	private static HashMap<String,Class<? extends ILinkAction>> map 
	= new HashMap<String, Class<? extends ILinkAction>>();
	
	private ILogger log = LogManager.getLogger(getClass());
	private String target;
	private String source;
	private String action;
	private Properties properties = new Properties();
	
	/** Registers a class to be used as a link action */
	public static void registerLinkAction(Class<? extends ILinkAction> klass){
		map.put(klass.getSimpleName(), klass);
	}
	
	/** Unregisters a link action class */
	public static void unregisterLinkAction(Class<? extends ILinkAction> klass){
		map.remove(klass.getSimpleName());
	}
	
	/** CONSTRUCTS a link instance, the HTML viewer will pass in the link and then set the source */
	public LinkTask(String target) {
		this.target = target;
	}

	/** 
	 * The viewer will the instruct TaskManger to execute this link as a spawned thread,
	 * Task manager thread will then execute this method to perform the task.
	 * 
	 * This method parses the link, parses the parameters 
	 * ,locates the appropriate action class  
	 * and then executes the action.
	 */
	@Override
	public void executeTask() {
		//log.warn("Source:"+source);
		int ix = target.indexOf(ACTION);
		if(ix>0){
			target=target.substring(ix);
		}
		//log.info("Target="+target);
		ix = target.indexOf(":");
		int qx=target.indexOf("?");
		if(qx<0){
			action = target.substring(ix+1);
			//log.info("Action="+action);
		}else{
			//log.info("Action="+action);
			action = target.substring(ix+1,qx);
			String data = target.substring(qx+1);
			String[] vars = data.split("&");
			for(String v:vars){
				ix = v.indexOf("=");
				if(ix>0){
					String key = v.substring(0,ix).trim();
					if(key.endsWith(";")){
						key = key.substring(0,key.length()-1);
					}
					String value = v.substring(ix+1).trim();
					properties.put(key.toUpperCase(),value);
					//log.info(key+" = " + value);
				}
			}
		}
		Class<? extends ILinkAction> k = map.get(action+"Action");
		if(k==null){
			log.error("No class registered for action "+action);
		}else{
			try {
				ILinkAction la = k.newInstance();
				la.doAction(this);
			} catch (Exception e) {
				log.error("Problem executing action "+target,e);
			}
		}
	}

	/** Used by viewer to set source (page from which link was clicked) */
	public void setSource(String source) {
		this.source = source;
	}
	
	/** Provides access to the passed parameters (as Int)*/
	public int getInt(String name){
		String v = properties.getProperty(name.toUpperCase());
		int r = -1;
		try {
			r = Integer.parseInt(v);
		} catch (NumberFormatException e) {
			log.warn("Problem recalling integer parameter '"+name+"', v='"+v+"'");
			System.out.println(this.toString());
		}
		return r;
	}
	
	/** Provides access to the passed parameters (as String)*/
	public String getString(String name){
		return properties.getProperty(name.toUpperCase());
	}

	public ILogger getLog() {
		return log;
	}

	public void setLog(ILogger log) {
		this.log = log;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getSource() {
		return source;
	}

	@Override
	public String toString() {
		return "LinkTask [target=" + target + ", source=" + source + ", action=" + action + ", properties=" + properties + "]";
	}


}
