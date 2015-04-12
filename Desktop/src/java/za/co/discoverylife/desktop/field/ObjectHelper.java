package za.co.discoverylife.desktop.field;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import za.co.discoverylife.desktop.util.MRUHashMap;

/**
 * Provides access to data object via field names.
 * 
 * @author Anton
 * 
 */
public class ObjectHelper {
	/** MostRecentlyUsed Cache map of class vs ObjectHelper */
	private static MRUHashMap<Class<?>, ObjectHelper> map
		= new MRUHashMap<Class<?>, ObjectHelper>(100);
	
	/** Returns an ObjectHelper for the provided class */
	public static ObjectHelper getHelper(Class<?> klass){
		ObjectHelper oh = map.get(klass);
		if(oh==null){
			oh = new ObjectHelper(klass);
			map.put(klass, oh);
		}
		return oh;
	}
	
	/** Sets the maximum size for the ObjectHelper's MRU cache */
	public static void setMapSize(int size){
		map.setMaxEntries(size);
	}
	
	/** Copy a clone of source fields into matching fields in the target object */
	public static void copy(Object source,Object target) throws Exception{
		ObjectHelper ohSrc = getHelper(source.getClass());
		ObjectHelper ohTgt = getHelper(target.getClass());
		for(String fieldname:ohSrc.fldMap.keySet()){
			Object value = ohSrc.cloneField(source, fieldname);// get cloned value
			try {
				ohTgt.setFieldObject(target, fieldname, value);// set into target
			} catch (Exception e) {
				// target field not found or incompatible;
			}
		}
	}

	private HashMap<String, FieldHelper> fldMap;

	/**
	 * CONSTRUCT an Object Helper for the provided class, navigating up the
	 * extends tree to parent classes.
	 * 
	 * @param klass
	 *            Class to construct Helper for.
	 */
	private ObjectHelper(Class<?> klass) {
		this(klass, true);
	}

	/**
	 * CONSTRUCT an Object Helper for the provided class.
	 * 
	 * @param klass
	 *            Class to construct helper for
	 * @param doParentClass
	 *            true=navigate up the parent classes
	 */
	private ObjectHelper(Class<?> klass, boolean doParentClass) {
		fldMap = new HashMap<String, FieldHelper>();
		Class<?> k = klass;
		while (k != null) {
			Field[] flds = k.getDeclaredFields();
			for (Field f : flds) {
				// skip static fields and transient fields
				int mod = f.getModifiers();
				if (Modifier.isTransient(mod) || Modifier.isStatic(mod)) {
					continue;
				}
				FieldHelper fa = new FieldHelper(f);
				fldMap.put(f.getName(), fa);
			}
			if (!doParentClass) {
				break;
			}
			k = k.getSuperclass();
			if (k == null || k.equals(Object.class)) {
				break;
			}
		}
	}
	
	

	/**
	 * Returns a clone copy of the sourceObject field value. <br>
	 * (new instance/object, not a duplicate pointer)
	 * 
	 * @param sourceObject
	 *            Object to copy the field from
	 * @param fieldname
	 *            name of the field to copy
	 * @return Replica field value object
	 * @throws Exception
	 */
	public Object cloneField(Object sourceObject, String fieldname)
			throws Exception {
		FieldHelper fh = findField(fieldname);
		if (fh.isSimple()) {
			return fh.getValueAsObject(fh.getStringFromField(sourceObject));
		}
		return null;
	}

	/**
	 * Returns the value of the named field, on the specified object. 
	 * The value is automatically wrapped in an object if it has a primitive type.
	 * 
	 * @param sourceObject
	 *            Object to read the field from
	 * @param fieldname
	 *            name of the field to read
	 * @return String representation of the filed value
	 * @throws Exception
	 *             Any error
	 */
	public Object getFieldObject(Object sourceObject, String fieldname)
			throws Exception {
		FieldHelper fh = findField(fieldname);
		return fh.getField().get(sourceObject);
	}

	/**
	 * Sets the named field in the target object from the string representation
	 * provided
	 * 
	 * @param targetObject
	 *            object to have it's field set
	 * @param fieldname
	 *            name of the field to set
	 * @param value
	 *            String representation of the new field value
	 * @throws Exception
	 *             Any errors
	 */
	public void setFieldObject(Object targetObject, String fieldname,
			Object value) throws Exception {
		FieldHelper fh = findField(fieldname);
		fh.getField().set(targetObject, value);
	}

	/**
	 * Returns the FieldHelper for the named field, Throws an exception if the
	 * field name is not found
	 * 
	 * @param fieldname
	 *            name of the field to access
	 * @return FieldHelper for the field
	 * @throws Exception
	 *             If the field name is not found
	 */
	private FieldHelper findField(String fieldname) throws Exception {
		FieldHelper fh = findField(fieldname);
		return fh;
	}

}
