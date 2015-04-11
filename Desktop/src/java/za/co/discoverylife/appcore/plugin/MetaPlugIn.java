package za.co.discoverylife.appcore.plugin;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Holds information about a Plug In item.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaPlugIn {

	/** Type of plug-in being registered */
	String type() default IPlugInConstants.PLUGIN_TOOLS_MENU;

	/** Description of the plugin / module */
	String description();
	
	/** Name of help file for the plugin */
	String helpName() default "";
}
