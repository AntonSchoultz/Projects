package za.co.discoverylife.appcore.field;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import za.co.discoverylife.desktop.util.DateTime;



/**
 * Provides access to fields within objects , via an editable string value.
 * Persist versions are available, which will encode sensitive information. By default
 * any field whose name contains 'password' (case-in-sensitive) will be encoded.
 * Sensitive fields may be marked with 'encrypt=true' in the @FieldInfo tag
 * 
 * @author anton11
 */
public class FieldAccessor implements IEditable, IFieldTypes
{

  private static final String URL_ENCODING_SET = "UTF-8";
  protected Object object;
  protected String fieldName;
  protected boolean isEncoded = false;
  protected MetaFieldInfo fldInfo;

  protected Field fld;
  protected String editedValue;

  /**
   * CONSTRUCTS a new field accessor for the provided object and field name
   * 
   * @param object
   * @param fieldName
   * @throws FieldAccessException
   *           if data object is null, or field name is invalid
   */
  public FieldAccessor(Object object, String fieldName) throws FieldAccessException
  {
    super();
    if ( object == null )
    {
      throw new FieldAccessException("Data object may not be null");
    }
    this.object = object;
    this.fieldName = fieldName;
    fld = findField(object.getClass(), fieldName);
    if ( fld == null )
    {
      throw new FieldAccessException("Field name '" + fieldName + "' not found in class " + object.getClass().getName());
    }
    fld.setAccessible(true);
    fldInfo = fld.getAnnotation(MetaFieldInfo.class);
    isEncoded = (fieldName.toUpperCase().indexOf("PASSWORD") >= 0);
    if ( fldInfo != null )
    {
      isEncoded = isEncoded || fldInfo.encrypt();
    }
  }

  /** Returns true if the field is annotated */
  public boolean hasAnnotation()
  {
    return fldInfo != null;
  }

  /** Returns true if the filed was marked as read-only by the @FieldInfo.isReadOnly */
  public boolean isReadOnly()
  {
    if ( fldInfo != null )
    {
      return fldInfo.isReadOnly();
    }
    return false;
  }

  /** Copies the fields from this object to the target object */
  public void copyTo(Object target)
  {
    try
    {
      fld.set(target, fld.get(object));
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Create a new instance for this field type
   * 
   * @return filed object
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  public Object getNewFieldObject() throws InstantiationException, IllegalAccessException
  {
    return fld.getType().newInstance();
  }

  /** Returns true if the field was marked as being a directory with @FieldInfo.isFolder */
  public boolean isFolder()
  {
    if ( fldInfo != null )
    {
      return fldInfo.isFolder();
    }
    return false;
  }

  /**
   * Returns the Class of the field
   * 
   * @return field type/class
   */
  public Type getFieldClassType()
  {
    return fld.getGenericType();
  }

  /** set edit value from the object field value */
  public void undo()
  {
    editedValue = getStringFromField();
  }

  /**
   * Sets the data field to the value given in MetaFieldInfo.initial()
   */
  public void reset()
  {
    if ( fldInfo != null )
    {
      String val = fldInfo.initial();
      if ( val.length() > 0 )
      {
        setFieldFromString(val.trim());
      }
    }
  }

  /** Commit the edit value to the object */
  public void commit()
  {
    setFieldFromString(editedValue);
  }

  /** Return label from @FieldInfo annotation (default to field name if no annotation) */
  public String getLabel()
  {
    String lbl = null;
    if ( fldInfo != null )
    {
      lbl = fldInfo.label();
    }
    if ( lbl == null || lbl.trim().length() == 0 )
    {
      lbl = fieldName;
    }
    return lbl;
  }

  /** Return hint from @FieldInfo annotation (default to field name if no annotation) */
  public String getHint()
  {
    if ( fldInfo != null )
    {
      return fldInfo.hint();
    }
    return getLabel();
  }

  /** Return icon name from @FieldInfo annotation */
  public String getIconName()
  {
    if ( fldInfo != null )
    {
      return fldInfo.icon();
    }
    return getLabel();
  }

  /** Return the field name */
  public String getFieldName()
  {
    return fld.getName();
  }

  /** Return the field name with first letter in upper case */
  public String getFieldNameUpperFirst()
  {
    String name = fld.getName();
    return toUpperFirst(name);
  }

  /**
   * Returns the string, converting first character to upper case.
   * @param name Name to convert
   * @return name with first character as Upper case
   */
  public static String toUpperFirst(String name)
  {
    StringBuffer sb = new StringBuffer(name);
    sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
    return sb.toString();
  }

  /** Returns the name of the options list */
  public String getListName()
  {
    return fldInfo.associatedName();
  }

  /** Returns the name of the options list */
  public int getDropRows()
  {
    return Math.max(0, fldInfo.dropRows());
  }

  /**
   * Return the field as an object
   * 
   * @return field object
   */
  public Object getFieldObject()
  {
    Object o = null;
    try
    {
      o = fld.get(object);
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }
    return o;
  }

  /** Return the generic type for a collection/compound field (type of objects) */
  public String getFieldInnerType()
  {
    String type = fld.getGenericType().toString();
    int ix = type.indexOf("<");
    if ( ix >= 0 )
    {
      type = type.substring(ix + 1, type.length() - 1);
    }
    return type;
  }

  /** Return collection type */
  public String getFieldOuterType()
  {
    String type = fld.getGenericType().toString();
    int ix = type.indexOf("<");
    if ( ix >= 0 )
    {
      type = type.substring(0, ix);
    }
    return type;
  }

  /**
   * Set the field from an Object
   * 
   * @param object
   *          to be set
   */
  public void setFieldObject(Object o)
  {
    try
    {
      fld.set(object, o);
    }
    catch (IllegalArgumentException e)
    {
      e.printStackTrace();
    }
    catch (IllegalAccessException e)
    {
      e.printStackTrace();
    }
  }

  /** Recursive method to locate the named field in the data class or it's ancestors */
  private Field findField(Class<?> k, String name)
  {
    if ( k == null )
      return null;
    Field f;
    try
    {
      f = k.getDeclaredField(name);
      if ( f != null )
        return f;
    }
    catch (Exception e)
    {
      //System.out.println("Field '" + name + "' not found in class " + k.getName()
      //+", trying parent class...");
    }
    return findField(k.getSuperclass(), name);
  }

  /** get field value as a String */
  public String getStringFromField()
  {
    try
    {
      if ( getFieldType() == TYPE_DATE )
      {
        DateTime dtm = new DateTime((Date) fld.get(object));
        return dtm.toString();
      }
      return String.valueOf(fld.get(object));
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return null;
    }
  }

  /** Returns true if this field is a numeric type */
  public boolean isNumeric()
  {
    boolean isNumber = false;
    switch (getFieldType())
    {
      case TYPE_SHORT :
      case TYPE_INT :
      case TYPE_LONG :
      case TYPE_FLOAT :
      case TYPE_DOUBLE :
        isNumber = true;
        break;
      default :
        isNumber = false;
        break;
    }
    return isNumber;
  }

  /**
   * Return the id of the field data type.
   * <ol start="0">
   * <li>TYPE_OBJECT</li>
   * <li>TYPE_STRING</li>
   * <li>TYPE_CHAR</li>
   * <li>TYPE_SHORT</li>
   * <li>TYPE_INT</li>
   * <li>TYPE_LONG</li>
   * <li>TYPE_DOUBLE</li>
   * <li>TYPE_FLOAT</li>
   * <li>TYPE_BOOLEAN</li>
   * <li>TYPE_FILE</li>
   * </ol>
   */
  public int getFieldType()
  {
    return getFieldType(fld.getType());
  }

  /**
   * Get field type code for the given class
   * 
   * @param fldClass
   * @return
   */
  public static int getFieldType(Class<?> fldClass)
  {
    int rtn = 0;
    for (int i = 1; i < CLASS.length; i++)
    {
      if ( fldClass.equals(CLASS[i]) )
      {
        rtn = i;
        break;
      }
    }
    for (int i = 1; i < CLASSJ.length; i++)
    {
      if ( CLASSJ[i].isAssignableFrom(fldClass) )
      {
        rtn = i;
        break;
      }
    }
    return rtn;
  }

  /**
   * Returns true if the field is considered simple (can be represented by a simple unstructured
   * string value)
   * 
   * @param fldClass
   * @return
   */
  public static boolean isSimple(Class<?> fldClass)
  {
    return (getFieldType(fldClass) > 0);
  }

  /**
   * Sets the specified Field of the object from the string value provided
   * 
   * @param value
   *          String value to set field to
   * @throws FieldAccessException
   */
  public void setFieldFromString(String value)
  {
    try
    {
      if ( value == null )
      {
        return;
      }
      if ( "null".equals(value) )
      {
        fld.set(object, null);
        return;
      }
      Object v = getValueAsObject(value);
      if ( v != null )
      {
        fld.set(object, v);
      }
      else
      {
        throw new FieldAccessException("Could not set " + fieldName + " from " + value);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
      editedValue = null;
    }
    editedValue = value;
  }

  /**
   * convert the simple string representation into the appropriate object
   * 
   * @param value
   * @return
   */
  @SuppressWarnings({"rawtypes", "unchecked"})
  private Object getValueAsObject(String value)
  {
    Object v = null;
    Class<? extends Object> fldClass = fld.getType();
    int ty = getFieldType(fldClass);

    switch (ty)
    {
      case TYPE_COLLECTION :
        String[] sa = value.substring(1, value.length() - 1).split(",");
        try
        {
          Class innerType = Class.forName(getFieldInnerType());
          if ( innerType == java.lang.String.class )
          {
            Collection<String> col = (Collection<String>) getFieldObject();
            v = col;
            for (String s : sa)
            {
              s = s.trim();
              if ( !col.contains(s) )
              {
                col.add(s);
              }
            }
          }
        }
        catch (Exception e1)
        {
          e1.printStackTrace();
        }
        break;
      case TYPE_DATE :
        DateTime dt = new DateTime(value);
        v = dt.getAsDate();
        //System.out.println("FieldAccessor:489 DateCvt '" + editedValue + "' -> " + ((Date) v).toString());
        break;

      case TYPE_OBJECT :
      case TYPE_STRING :
        v = value;
        break;
      case TYPE_CHAR :
        v = (char) Integer.parseInt(value);
        break;
      case TYPE_SHORT :
        v = Short.parseShort(value);
        break;
      case TYPE_INT :
        v = Integer.parseInt(value);
        break;
      case TYPE_LONG :
        v = Long.parseLong(value);
        break;
      case TYPE_DOUBLE :
        v = Double.parseDouble(value);
        break;
      case TYPE_FLOAT :
        v = Float.parseFloat(value);
        break;
      case TYPE_BOOLEAN :
        v = Boolean.parseBoolean(value);
        break;
      case TYPE_FILE :
        v = new File(value);
        break;
    }
    if ( v == null )
    {
      // type not known - try constructor which takes a string value
      try
      {
        Constructor<? extends Object> constr = fldClass.getConstructor(String.class);
        v = constr.newInstance(value);
      }
      catch (Exception e)
      {
        // e.printStackTrace();
      }
    }
    return v;
  }

  /** Returns the String representation of the editable value */
  public String getEditedValue()
  {
    return editedValue;
  }

  /** Returns the edited value as an object (of correct type) */
  public Object getEditedValueAsObject()
  {
    return getValueAsObject(editedValue);
  }

  /** Sets the String representation of the editable value */
  public void setEditedValue(String editedValue)
  {
    this.editedValue = editedValue;
  }

  /**
   * Return a string representation of the field, suitable for persisting This includes 'encoding'
   * of password fields.
   * 
   * @return
   * @throws FieldAccessException
   */
  public String getPersistString() throws FieldAccessException
  {
    String value = getStringFromField();
    if ( isEncoded )
    {
      try
      {
        value = codec(value);
        value = URLEncoder.encode(value, URL_ENCODING_SET);
      }
      catch (UnsupportedEncodingException e)
      {
        e.printStackTrace();
      }
    }
    return value;
  }

  /**
   * Sets the field value form its' persistence string representation. This includes 'decoding'
   * passwords.
   * 
   * @param persitString
   * @throws FieldAccessException
   */
  public void setFromPersistString(String persitString) throws FieldAccessException
  {
    if ( isEncoded )
    {
      // decode persitString
      try
      {
        persitString = URLDecoder.decode(persitString, URL_ENCODING_SET);
      }
      catch (UnsupportedEncodingException e)
      {
        e.printStackTrace();
      }
      persitString = codec(persitString);
    }
    // set value
    setFieldFromString(persitString);
  }

  /**
   * Method used to code/decode sensitive fields. Very simple, but at least it's not plain-text. :)
   */
  private static String codec(String in)
  {
    if ( in == null )
    {
      return null;
    }
    int n = 0x13;
    char[] ca = in.toCharArray();
    for (int i = 0; i < ca.length; i++)
    {
      ca[i] = (char) (ca[i] ^ (n + (i & 0x7)));
    }
    return new String(ca);
  }

  /**
   * Returns a string representation of key fields in this object for debugging
   */
  @Override
  public String toString()
  {
    return "FieldAccessor [object=" + object + ", fieldName=" + fieldName + ", fldInfo=" + fldInfo + ", fld=" + fld
        + ", editedValue=" + editedValue + "]";
  }

  /**
   * Returns a list of fieldAccessors to access all the non-static and non-transient fields of the
   * supplied object.
   * 
   * @param obj
   *          Object to get list of accessors for
   * @return List{FieldAccessor} for non-static,non-transient fields
   */
  public static List<FieldAccessor> listAccessorsForObject(Object obj)
  {
    return listAccessorsForObject(obj, true);
  }

  /**
   * Returns a list of fieldAccessors to access all the non-static and non-transient fields of the
   * supplied object.
   * 
   * @param obj
   *          Object to get list of accessors for
   * @return List{FieldAccessor} for non-static,non-transient fields
   */
  public static List<FieldAccessor> listAccessorsForObject(Object obj, boolean doParentClass)
  {
    if ( obj == null )
      return null;
    List<FieldAccessor> list = new ArrayList<FieldAccessor>();
    Class<?> k = obj.getClass();
    while (k != null)
    {
      Field[] flds = k.getDeclaredFields();
      for (Field f : flds)
      {
        // skip static fields and transient fields
        int mod = f.getModifiers();
        if ( Modifier.isTransient(mod) || Modifier.isStatic(mod) )
        {
          continue;
        }
        try
        {
          FieldAccessor fa = new FieldAccessor(obj, f.getName());
          list.add(fa);
        }
        catch (FieldAccessException e)
        {
          e.printStackTrace();
        }
      }
      if ( !doParentClass )
      {
        break;
      }
      k = k.getSuperclass();
      if ( k == null || k.equals(Object.class) )
      {
        break;
      }
    }
    return list;
  }

  /**
   * Returns a Field accessor for the named field in the provided object
   * 
   * @param data
   *          Object whose field is to be accessed
   * @param collectionFieldName
   *          Name of the field to access
   * @return
   */
  public static FieldAccessor getAccessor(Object data, String collectionFieldName)
  {
    List<FieldAccessor> list = listAccessorsForObject(data);
    for (FieldAccessor fa : list)
    {
      if ( fa.getFieldName().equalsIgnoreCase(collectionFieldName) )
      {
        return fa;
      }
    }
    return null;
  }

  /**
   * Return a width for this field in characters.
   * the width may be specified in the MeatInfo as width=n.
   * If the width annotation is absent, or set to 0, then
   * a default width is assigned.
   * 
   * @return width for this field in characters
   */
  public int getSize()
  {
    int w = fldInfo.width();
    if ( w == 0 )
    {
      switch (this.getFieldType())
      {
        case TYPE_BOOLEAN :
          w = 2;
          break;
        case TYPE_DATE :
          w = 11;
          break;
        case TYPE_OBJECT :
        case TYPE_STRING :
          w = 50;
          break;
        case TYPE_CHAR :
          w = 1;
          break;
        case TYPE_SHORT :
        case TYPE_INT :
          w = 10;
          break;
        case TYPE_LONG :
        case TYPE_FLOAT :
        case TYPE_DOUBLE :
          w = 20;
          break;
        case TYPE_FILE :
          w = 50;
          break;
        default :
          w = 5;
          break;
      }
    }
    return w;
  }
}
