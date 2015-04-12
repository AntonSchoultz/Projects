package za.co.discoverylife.desktop.field;

import java.lang.reflect.Field;

public class FieldHelper implements IFieldTypes{
	public Field field;
	public int fieldTypeCd;

	public FieldHelper(Field field) {
		this.field = field;
		this.field.setAccessible(true);
		fieldTypeCd = getFieldType(field.getClass());
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

	public Field getField() {
		return field;
	}

}
