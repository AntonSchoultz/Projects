package za.co.discoverylife.appcore.task;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use this to provide Menu Title information for models.
 * 
 * @author Anton Schoultz
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaMenu {

	/** Label to be used in Menu title */
	String label() default "";

	/** Icon to be used for menu title */
	String icon() default "";

	/** Tool-tip / additional description */
	String hint() default "";

}
