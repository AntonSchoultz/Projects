package za.co.discoverylife.appcore.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Holds information about a field which is used for
 * building screens. Used to mark which fields to display edit.
 * 
 * @author anton11
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MetaFieldInfo {

	/** Label to be used on screen for this field */
	String label() default "";

	/** Tool-tip / additional description */
	String hint() default "";

	/** initial string value for the field */
	String initial() default "";

	/**
	 * options list name
	 * - for drop-down selections key[=description]
	 * - for Sets, this is the name of the buddy field
	 */
	String associatedName() default "";

	/** Size for drop down list, 0=default */
	int dropRows() default 0;
	
	/** width of field in characters (0=assigns default) */
	int width() default 0;

	/** Icon to be used for this field (for Sets) */
	String icon() default "";

	/** True if the text should be encrypted when persisted */
	boolean encrypt() default false;

	/** For file fields, if true selecting Directory/Folder, false is for files */
	boolean isFolder() default false;

	/** Marks field as Display only */
	boolean isReadOnly() default false;

}
