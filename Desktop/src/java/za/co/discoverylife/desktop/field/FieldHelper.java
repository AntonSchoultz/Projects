package za.co.discoverylife.desktop.field;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
//import java.util.Collection;

import java.util.Date;

import za.co.discoverylife.desktop.util.DateTime;

public class FieldHelper implements IFieldTypes {
	public Field field;
	public int fieldTypeCd;

	public FieldHelper(Field field) {
		this.field = field;
		this.field.setAccessible(true);
		fieldTypeCd = getFieldType(field.getClass());
	}

	/** Returns true if the field is a 'simple'/'native' type */
	public boolean isSimple() {
		return fieldTypeCd > 0 && fieldTypeCd < TYPE_COLLECTION;
	}

	/** get field value as a String */
	public String getStringFromField(Object object) {
		try {
			if (fieldTypeCd == TYPE_DATE) {
				DateTime dtm = new DateTime((Date) field.get(object));
				return dtm.toString();
			}
			return String.valueOf(field.get(object));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Sets the specified Field of the object from the string value provided
	 */
	public void setFieldFromString(Object targetObject, String value) {
		try {
			if (value == null) {
				return;
			}
			if ("null".equals(value)) {
				field.set(targetObject, null);
				return;
			}
			Object v = getValueAsObject(value);
			if (v != null) {
				field.set(targetObject, v);
			} else {
				throw new Exception("Could not set " + field.getName()
						+ " from " + value);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * convert the simple string representation into the appropriate object
	 * 
	 * @param value
	 * @return
	 */
	public Object getValueAsObject(String value) {
		Object v = null;
		Class<? extends Object> fldClass = field.getType();
		int ty = getFieldType(fldClass);

		switch (ty) {
		// case TYPE_COLLECTION :
		// String[] sa = value.substring(1, value.length() - 1).split(",");
		// try
		// {
		// Class innerType = Class.forName(getFieldInnerType());
		// if ( innerType == java.lang.String.class )
		// {
		// Collection<String> col = (Collection<String>) getFieldObject();
		// v = col;
		// for (String s : sa)
		// {
		// s = s.trim();
		// if ( !col.contains(s) )
		// {
		// col.add(s);
		// }
		// }
		// }
		// }
		// catch (Exception e1)
		// {
		// e1.printStackTrace();
		// }
		// break;
		case TYPE_DATE:
			DateTime dt = new DateTime(value);
			v = dt.getAsDate();
			break;
		case TYPE_OBJECT:
		case TYPE_STRING:
			v = value;
			break;
		case TYPE_CHAR:
			v = (char) Integer.parseInt(value);
			break;
		case TYPE_SHORT:
			v = Short.parseShort(value);
			break;
		case TYPE_INT:
			v = Integer.parseInt(value);
			break;
		case TYPE_LONG:
			v = Long.parseLong(value);
			break;
		case TYPE_DOUBLE:
			v = Double.parseDouble(value);
			break;
		case TYPE_FLOAT:
			v = Float.parseFloat(value);
			break;
		case TYPE_BOOLEAN:
			v = Boolean.parseBoolean(value);
			break;
		case TYPE_FILE:
			v = new File(value);
			break;
		}
		if (v == null) {
			// type not known - try constructor which takes a string value
			try {
				Constructor<? extends Object> constr = fldClass
						.getConstructor(String.class);
				v = constr.newInstance(value);
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		return v;
	}

	/**
	 * Return the field as an object
	 * 
	 * @return field object
	 */
	public Object getFieldAsObject(Object object) {
		Object o = null;
		try {
			o = field.get(object);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return o;
	}

	/**
	 * Return the generic type for a collection/compound field (type of objects)
	 */
	public String getFieldInnerType() {
		String type = field.getGenericType().toString();
		int ix = type.indexOf("<");
		if (ix >= 0) {
			type = type.substring(ix + 1, type.length() - 1);
		}
		return type;
	}

	/** Return collection type */
	public String getFieldOuterType() {
		String type = field.getGenericType().toString();
		int ix = type.indexOf("<");
		if (ix >= 0) {
			type = type.substring(0, ix);
		}
		return type;
	}

	/**
	 * Get field type code for the given class
	 * 
	 * @param fldClass
	 * @return
	 */
	public static int getFieldType(Class<?> fldClass) {
		int rtn = 0;
		for (int i = 1; i < CLASS.length; i++) {
			if (fldClass.equals(CLASS[i])) {
				rtn = i;
				break;
			}
		}
		for (int i = 1; i < CLASSJ.length; i++) {
			if (CLASSJ[i].isAssignableFrom(fldClass)) {
				rtn = i;
				break;
			}
		}
		return rtn;
	}

	public Field getField() {
		return field;
	}

}
