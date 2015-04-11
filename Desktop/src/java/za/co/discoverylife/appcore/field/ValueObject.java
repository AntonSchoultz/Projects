package za.co.discoverylife.appcore.field;

import java.util.List;
import java.util.Properties;

import za.co.discoverylife.appcore.table.DataRow;

/**
 * Parent class for data objects - which are copy able and clone able
 * 
 * @author anton11
 */
public abstract class ValueObject
    implements ICopyable
{
  @Override
  public Object clone()
  {
    Object obj = null;
    try
    {
      obj = super.clone();
    }
    catch (CloneNotSupportedException e)
    {
      System.err.println(getClass().getName() + " does not support clone !!");
    }
    return obj;
  }

  /** Copies values from one object to another */
  public final void copyTo(Object other)
  {
    for (FieldAccessor fa : getAccessorList())
    {
      fa.copyTo(other);
    }
  }

  /** resets fields to default value from metaInfo */
  public void reset()
  {
    for (FieldAccessor fa : getAccessorList())
    {
      fa.reset();
    }
  }

  /** 
   * Returns value object fields as a property map of string values.
   * 
   * Properties are named as the variable name with first letter in uppercase.
   *
   * @return
   */
  public Properties getAsProperties()
  {
    Properties props = new Properties();
    for (FieldAccessor fa : getAccessorList())
    {
      props.put(fa.getFieldNameUpperFirst(), fa.getStringFromField());
    }
    return props;
  }

  /** 
   * Sets object field values from properties
   * 
   * Properties are named as the variable name with first letter in uppercase.
   *
   * @param props
   */
  public void setFromProperties(Properties props)
  {
    if ( props != null )
    {
      for (FieldAccessor fa : getAccessorList())
      {
        String name = fa.getFieldNameUpperFirst();
        String value = props.getProperty(name);
        if ( value != null )
        {
          fa.setFieldFromString(value);
        }
      }
    }
  }

  /** Returns a list of FieldAccessors for this object */
  public List<FieldAccessor> getAccessorList()
  {
    return FieldAccessor.listAccessorsForObject(this);
  }

  /** Reads all fields and creates a DataRow from them */
  public DataRow getAsDataRow()
  {
    DataRow row = new DataRow();
    for (FieldAccessor fa : getAccessorList())
    {
      row.addCell(fa.getStringFromField(), fa.getSize(), fa.isNumeric());
    }
    return row;
  }

  protected void wr(String s)
  {
    System.out.println(getClass().getSimpleName() + ":" + s);
  }

}
