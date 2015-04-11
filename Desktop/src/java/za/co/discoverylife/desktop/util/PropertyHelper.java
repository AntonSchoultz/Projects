package za.co.discoverylife.desktop.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Utility functions related to properties objects.
 * 
 * @author anton11
 */
public class PropertyHelper
{

  // SYSTEM PROPERTIES - some useful ones ? :)
  // java.home = "C:\Program Files\Java\jre1.5.0"
  // sun.boot.library.path = "C:\Program Files\Java\jre1.5.0\bin"
  // user.home=d:\Users Documents\anton11
  // user.name=anton11
  // sun.cpu.endian=little
  // os.arch=x86

  public static final String OS_JAVA_HOME = "java.home";
  public static final String OS_BOOT_LIBRARY_PATH = "sun.boot.library.path";
  public static final String OS_USER_HOME = "user.home";
  public static final String OS_USER_NAME = "user.name";
  public static final String OS_USER_DIR = "user.dir";
  public static final String OS_CPU_ENDIAN = "sun.cpu.endian";
  public static final String OS_ARCHITECTURE = "os.arch";

  /** Echos some key System properties to System.out **/
  public static void echoSystemProperties()
  {
    getSystemProperty(OS_ARCHITECTURE);
    getSystemProperty(OS_BOOT_LIBRARY_PATH);
    getSystemProperty(OS_CPU_ENDIAN);
    getSystemProperty(OS_JAVA_HOME);
    getSystemProperty(OS_USER_DIR);
    getSystemProperty(OS_USER_HOME);
    getSystemProperty(OS_USER_NAME);
  }

  /** Returns true if OS is a 32 bit system */
  public static boolean is32bit()
  {
    String os = getSystemProperty(OS_ARCHITECTURE);
    // add valid 32bit OS Architecture names here, separated by spaces
    return (" x86 i386 ".indexOf(os) > 0);

  }

  /** Returns the named System property (also echoed to Sysem.out) */
  public static String getSystemProperty(String key)
  {
    String value = System.getProperty(key);
    //System.out.println("## " + key + "='" + value + "'");
    return value;
  }

  public static String getSystemPropertiesAsString()
  {
    StringBuilder sb = new StringBuilder();
    Properties sp = System.getProperties();
    for (Entry<Object, Object> item : sp.entrySet())
    {
      sb.append((String) item.getKey());
      sb.append("=");
      sb.append((String) item.getValue());
      sb.append("\r\n");
    }
    return sb.toString();
  }

  /**
   * loads properties from the file specified
   * 
   * @throws IOException
   */
  public static Properties loadProperties(File propFile) throws IOException
  {
    Properties props = new Properties();
    try
    {
      FileInputStream fis = new FileInputStream(propFile);
      props.load(fis);
      fis.close();
    }
    catch (Exception e)
    {
      // e.printStackTrace();
      props = new Properties();
    }
    return props;
  }

  /**
   * Writes the properties supplied into the file specified.
   * 
   * @param props
   *          Properties object to be written to disk.
   * @param fileSpec
   *          String full path and name of the file to be written.
   * @param title
   *          String title to be written into the properties file.
   * @throws IOException
   * @throws IOException
   *           if there was a problem closing the file.
   */
  public static void storeProperties(Properties props, File propFile, String title) throws IOException
  {
    FileOutputStream fos = null;
    try
    {
      fos = new FileOutputStream(propFile);
      props.store(fos, title);
      fos.flush();
    }
    finally
    {
      if ( fos != null )
      {
        try
        {
          fos.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
  }

//  /** Store an object as a properties file */
//  public static void storeObject(Object obj, File f)
//  {
//    String name = obj.getClass().getSimpleName();
//    Properties p = PropertyHelper.readObjectProperties(obj);
//    try
//    {
//      storeProperties(p, f, name + " state");
//    }
//    catch (IOException e)
//    {
//      e.printStackTrace();
//    }
//  }

//  /** Recall an object's state from properties file */
//  public static Object recallObject(Object obj, File f) throws IOException
//  {
//    Properties p = loadProperties(f);
//    setObjectFromProperties(obj, p, "");
//    return obj;
//  }

//  /**
//   * Read object's fields and return as properties
//   * 
//   * @param object
//   * @return
//   */
//  public static Properties readObjectProperties(Object object)
//  {
//    Properties p = new Properties();
//    // System.out.println("readObjectProperties:" + object);
//    List<FieldAccessor> lst = FieldAccessor.listAccessorsForObject(object);
//    for (FieldAccessor fa : lst)
//    {
//      if ( fa.getFieldType() != FieldAccessor.TYPE_OBJECT )
//      {
//        try
//        {
//          String key = fa.getFieldName();
//          String value = fa.getPersistString();
//          p.put(key, value);
//          // System.out.println("\t:" + key + "=" + value);
//        }
//        catch (FieldAccessException e)
//        {
//          e.printStackTrace();
//        }
//      }
//    }
//    return p;
//  }

//  public static void setObjectFromProperties(Object object, Properties props, String prefix)
//  {
//    List<FieldAccessor> lst = FieldAccessor.listAccessorsForObject(object);
//    // System.out.println("setObjectFromProperties:" + props);
//    for (FieldAccessor fa : lst)
//    {
//      if ( fa.getFieldType() != FieldAccessor.TYPE_OBJECT )
//      {
//        try
//        {
//          String key = prefix + fa.getFieldName();
//          String value = props.getProperty(key);
//          if ( value != null )
//          {
//            // System.out.println("\t:" + key + "=" + value);
//            fa.setFromPersistString(value);
//          }
//          else
//          {
//            fa.reset();
//            // System.out.println("\t:" + key + " RESET");
//          }
//        }
//        catch (FieldAccessException e)
//        {
//          e.printStackTrace();
//        }
//      }
//    }
//  }

  /** Expands ${VAR} expressions */
  public static String expandPropertyValues(Properties props, String inValue)
  {
    StringBuffer sb = new StringBuffer();
    int bx = 0;
    int ix = inValue.indexOf("${", bx);
    boolean resolved = false;
    while (ix >= 0)
    {
      sb.append(inValue.substring(bx, ix));// everything up to ${
      int ex = inValue.indexOf("}", ix);
      if ( ex > ix )
      {
        String key = inValue.substring(ix + 2, ex);
        String value = props.getProperty(key);
        if ( value != null )
        {
          resolved = true;
          sb.append(value);
        }
        else
        {
          // not found
          sb.append("${").append(key).append("}");
        }
        bx = ex + 1;
      }
      else
      {
        sb.append(inValue.substring(bx, ix + 2));
        bx = ix + 2;
      }
      if ( bx >= inValue.length() )
      {
        break;
      }
      ix = inValue.indexOf("${", bx);
    }
    if ( bx < inValue.length() )
    {
      sb.append(inValue.substring(bx));
    }
    if ( resolved )
    {
      return expandPropertyValues(props, sb.toString());
    }
    return sb.toString();
  }

}
