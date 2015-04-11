package za.co.discoverylife.appcore.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import za.co.discoverylife.appcore.field.FieldAccessException;
import za.co.discoverylife.appcore.field.FieldAccessor;
import za.co.discoverylife.appcore.field.IFieldTypes;
import za.co.discoverylife.appcore.xml.XTag;
import za.co.discoverylife.appcore.xml.XTagParser;
import za.co.discoverylife.desktop.util.FileHelper;

/**
 * XML utilities for converting between Object and XML.
 * 
 * <ul>
 * <li>@{@link #encode(Object)} encodes object into xml
 * <li>@{@link #decode(XTag)} decode xml back to object
 * </ul>
 * <b>Note:</b>This may not work for all possible object graphs.
 * <p>
 * It will try to navigate down object graphs and does try to handle collections and maps.
 * 
 * @author anton11
 */
public class XmlUtil
    implements IFieldTypes
{

  private static final String PREFIX = "_";// ":";
  private static final String CLASS = PREFIX + "CLASS";
  private static final String ENTRY = PREFIX + "ENTRY";
  private static final String MAP = PREFIX + "MAP";
  private static final String COLLECTION = PREFIX + "COLLECTION";
  private static final String VALUE = "value";
  private static final String NULL = PREFIX + "NULL";

  private static final String UTF_8 = "UTF-8";

  private HashMap<Class<? extends Object>, String> tagMap = null;
  private Map<String, Class<? extends Object>> classMap = null;

  public boolean addClass = true;

  /** CONSTRUCTOR that accepts a flag to indicate if class info should be output */
  public XmlUtil(boolean addClass)
  {
    this.addClass = addClass;
  }

  /** CONSTRUCTOR - default */
  public XmlUtil()
  {
  }

  /**
   * Store an object as XML file
   * 
   * @param obj
   * @param fp
   */
  public void storeObject(Object obj, File fp)
  {
    XTag doc = encode(obj);
    FileHelper.fileWrite(doc.toString(), fp);
  }

  /**
   * Encodes an object into an XML representation (no class data)
   * 
   * @param obj
   * @return
   */
  public XTag encodeBare(Object obj)
  {
    if ( obj == null )
      return new XTag(NULL);
    boolean oldAddClass = addClass;
    addClass = false;
    initTagMap();
    XTag xt = encodeItem(obj, null);
    addClass = oldAddClass;
    return xt;
  }

  /**
   * Encodes an object into an XML representation
   * 
   * @param obj
   * @return
   */
  public XTag encode(Object obj)
  {
    if ( obj == null )
      return new XTag(NULL);
    initTagMap();
    return encodeItem(obj, null);
  }

  private void initTagMap()
  {
    tagMap = new HashMap<Class<? extends Object>, String>();
    tagMap.put(String.class, "String");
  }

  private void initClassMap()
  {
    classMap = new HashMap<String, Class<? extends Object>>();
    classMap.put("String", String.class);
  }

  private int xmlType(int fieldType)
  {
    return fieldType < TYPE_COLLECTION ? fieldType : TYPE_OBJECT;
  }

  private XTag encodeItem(Object obj, FieldAccessor fa)
  {
    if ( obj == null )
      return new XTag(NULL);
    // /System.out.println("#XmlUtil:encoding " + obj.toString());
    XTag tag = (fa == null) ? getClassTag(obj) : new XTag(fa.getFieldName());
    Class<? extends Object> objClass = obj.getClass();
    // encode simple objects like String, Float etc as their string equivalents
    int ft = xmlType(FieldAccessor.getFieldType(objClass));
    if ( ft != TYPE_OBJECT && ft != TYPE_COLLECTION )
    {
      tag.setAttribute(VALUE, String.valueOf(obj));
      return tag;
    }
    // have an object of some sort
    String objClassName = objClass.getCanonicalName();
    // String fldClassType = (fa == null) ? objClassName : fa.getFieldClassType().toString();
    XTag kid;
    // handle Collection
    if ( obj instanceof java.util.Collection )
    {
      // /System.out.println("#XmlUtil:encoding collection " + tag.getName());
      if ( addClass )
      {
        tag.setAttribute(COLLECTION, objClassName);
      }
      Collection<?> fldCol = (Collection<?>) obj;
      for (Object o : fldCol)
      {
        kid = encodeItem(o, null);
        tag.addChild(kid);
      }
      return tag;
    }
    // handle maps
    if ( obj instanceof java.util.Map )
    {
      // /System.out.println("#XmlUtil:encoding map " + tag.getName());
      if ( addClass )
      {
        tag.setAttribute(MAP, objClassName);
      }
      Map<?, ?> fldmap = (Map<?, ?>) obj;
      for (Object ok : fldmap.keySet())
      {
        kid = new XTag(ENTRY);
        XTag xKey = encodeItem(ok, null);
        kid.addChild(xKey);
        Object ov = fldmap.get(ok);
        XTag xVal = encodeItem(ov, null);
        kid.addChild(xVal);
        tag.addChild(kid);
      }
      return tag;
    }
    // members as attributes or child tags
    List<FieldAccessor> lst = FieldAccessor.listAccessorsForObject(obj);
    if ( lst != null )
    {
      // /System.out.println("#XmlUtil:encoding fields " + tag.getName());
      for (FieldAccessor f : lst)
      {
        if ( xmlType(f.getFieldType()) == TYPE_OBJECT )
        {
          Object co = f.getFieldObject();
          if ( co == null )
          {
            kid = new XTag(f.getFieldName());
            kid.setAttribute(NULL, "");
            tag.addChild(kid);
          }
          else
          {
            tag.addChild(encodeItem(co, f));//
          }
        }
        else
        {
          try
          {
            String value = urlEncode(f.getPersistString());//
            tag.setAttribute(f.getFieldName(), value);
          }
          catch (FieldAccessException e)
          {
            e.printStackTrace();
          }
        }
      }
    }
    return tag;
  }

  private XTag getClassTag(Object obj)
  {
    Class<? extends Object> k = obj.getClass();
    boolean def = false;
    String tagName = tagMap.get(k);
    if ( tagName == null )
    {
      tagName = k.getSimpleName();
      tagMap.put(k, tagName);
      def = true;
    }
    XTag tag = new XTag(tagName);
    if ( def && addClass )
    {
      tag.setAttribute(CLASS, k.getName());
    }
    return tag;
  }

  private Class<?> getTagClass(XTag tag)
      throws Exception
  {
    Class<?> k = classMap.get(tag.getName());
    if ( k == null )
    {
      String cn = tag.getAttribute(CLASS);
      if ( cn == null )
        throw new Exception("Could not find class for tag " + tag);
      k = Class.forName(cn);
      classMap.put(tag.getName(), k);
    }
    return k;
  }

  /**
   * recalls an object state from xml file
   * 
   * @param obj
   * @param fp
   * @throws Exception
   */
  public void recallObject(Object obj, File fp)
      throws Exception
  {
    XTag doc = XTagParser.parseFile(fp);
    initClassMap();
    decode(doc, obj);
  }

  /**
   * Decodes an XML representation of an object back into the object.
   * 
   * @param tag
   * @return
   * @throws Exception
   */
  private Object decode(XTag tag)
      throws Exception
  {
    if ( tag == null )
      return null;
    if ( tag.isRoot() )
    {
      initClassMap();
    }
    Class<?> k = getTagClass(tag);
    Object obj;
    try
    {
      obj = k.newInstance();
    }
    catch (Exception e)
    {
      throw new Exception("Could not create Object for " + tag.getName() + " [missing null constructor "
          + k.getSimpleName()
          + "(){ }  ? ]", e);
    }
    return decode(tag, obj);
  }

  private Object decode(XTag tag, FieldAccessor f)
      throws Exception
  {
    XTag kid = tag.findChild(f.getFieldName());
    Object obj = f.getFieldObject();
    if ( obj == null )
    {
      obj = f.getNewFieldObject();
    }
    return decode(kid, obj);
  }

  @SuppressWarnings("unchecked")
  private Object decode(XTag tag, Object obj)
      throws Exception
  {
    if ( tag == null || tag.getAttribute(NULL) != null )
    {
      return null;
    }
    if ( obj instanceof java.lang.String )
    {
      String val = tag.getAttribute(VALUE);
      return val;
    }
    if ( obj instanceof java.util.Collection )
    {
      String lstType = tag.getAttribute(COLLECTION);
      Class<?> kl = Class.forName(lstType);
      Collection<Object> lst = (Collection<Object>) kl.newInstance();
      for (XTag kt : tag.getChildren())
      {
        lst.add(decode(kt));
      }
      return lst;
    }
    if ( obj instanceof java.util.Map )
    {
      String mapType = tag.getAttribute(MAP);
      Class<?> km = Class.forName(mapType);
      Map<Object, Object> map = (Map<Object, Object>) km.newInstance();
      for (XTag et : tag.getChildren())
      {
        List<XTag> entryItems = et.getChildren();
        XTag tKey = entryItems.get(0);
        XTag tVal = entryItems.get(1);
        Object key = decode(tKey);
        Object val = decode(tVal);
        map.put(key, val);
      }
      return map;
    }

    List<FieldAccessor> lst = FieldAccessor.listAccessorsForObject(obj);
    if ( lst != null )
    {
      for (FieldAccessor f : lst)
      {
        if ( xmlType(f.getFieldType()) == TYPE_OBJECT )
        {
          Object co = decode(tag, f);
          f.setFieldObject(co);
        }
        else
        {
          try
          {
            String value = urlDecode(tag.getAttribute(f.getFieldName()));
            f.setFromPersistString(value);
          }
          catch (FieldAccessException e)
          {
            e.printStackTrace();
          }
        }
      }
    }
    else
    {
      System.err.println("--- object=" + obj);
    }
    return obj;
  }

  protected String urlEncode(String text)
  {
    if ( text == null )
    {
      return null;
    }
    try
    {
      return URLEncoder.encode(text, UTF_8);
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
      return text;
    }
  }

  protected String urlDecode(String text)
  {
    if ( text == null )
    {
      return null;
    }
    try
    {
      return URLDecoder.decode(text, UTF_8);
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
      return text;
    }
  }

}
