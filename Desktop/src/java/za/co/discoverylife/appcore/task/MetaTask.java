package za.co.discoverylife.appcore.task;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as being available as an action end-point.
 * Provides
 * <ul>
 * <li>label for a button caption
 * <li>hint for tool-tip
 * <li>icon name/alias for an icon
 * <li>help additional help information
 * </ul>
 * 
 * @author Anton Schoultz
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaTask {

	/**
	 * Sequence number for appearance.
	 * In menus, if this number "jumps" by more than one, a separator will be inserted before the item.
	 */
	int seqId() default 0;

	/** Label to be used on Button caption, place an underscore ahead of the accelerator character {NAME + MNEMONIC} */
	String label() default "";

	/** Tool-tip / additional description {SHORT_DESCRIPTION} */
	String hint() default "";

	/** Icon specifier Major[.minor] - { SMALL_ICON} */
	String icon() default "";

	/** Help specifier Page#anchor {LONG_DESCRIPTION} */
	String help() default "";

}
