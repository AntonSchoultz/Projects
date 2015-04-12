package za.co.discoverylife.desktop.field;

import java.io.File;
import java.util.Collection;
import java.util.Date;

/**
 * Codes for member types
 * 
 * @author anton11
 */
public interface IFieldTypes
{
	public static final int TYPE_OBJECT = 0;
	// the following are considered 'simple'
	public static final int TYPE_STRING = 1;
	public static final int TYPE_CHAR = 2;
	public static final int TYPE_SHORT = 3;
	public static final int TYPE_INT = 4;
	public static final int TYPE_LONG = 5;
	public static final int TYPE_DOUBLE = 6;
	public static final int TYPE_FLOAT = 7;
	public static final int TYPE_BOOLEAN = 8;
	//
	public static final int TYPE_FILE = 9;
	public static final int TYPE_DATE = 10;
	// compound
	public static final int TYPE_COLLECTION = 11;

	public static Class<?>[] CLASS = {
			Object.class, String.class, char.class, short.class, int.class
			, long.class, double.class, float.class, boolean.class
	};

	public static Class<?>[] CLASSJ = {
			Object.class, String.class, Character.class, Short.class, Integer.class
			, Long.class, Double.class, Float.class, Boolean.class
			, File.class
			, Date.class
			, Collection.class
	};

}