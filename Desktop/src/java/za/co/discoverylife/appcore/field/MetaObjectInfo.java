package za.co.discoverylife.appcore.field;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Holds information about a data object
 * which is used for building screens. 
 * 
 * @author anton11
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaObjectInfo {

	/** Label to be used on screen for this field */
	String label() default "";

	/** Tool-tip / additional description */
	String hint() default "";

}
