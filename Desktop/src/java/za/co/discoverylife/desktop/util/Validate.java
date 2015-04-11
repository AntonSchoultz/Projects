package za.co.discoverylife.desktop.util;

/**
 * Various validation methods (notNull etc)
 * 
 * @author Anton Schoultz
 */
public class Validate
{
  /** 
   * Throws an exception if the supplied object is null.
   * 
   * @param obj
   * @param varName
   * @throws Exception 
   */
  public static void notNull(Object obj, String varName) throws Exception
  {
    if ( obj == null )
    {
      if ( varName.indexOf(" ") < 0 )
      {
        throw new Exception(varName + " may not be null");
      }
      else
      {
        throw new Exception(varName);
      }
    }
  }

  /** Returns true is the string has a non-null, non-empty value */
  public static boolean hasValue(String str)
  {
    return !(str == null || str.length() == 0);
  }

  /**
   * Checks if string is null or zero length,
   * if so returns the defaultValue provided, otherwise
   * returns the string.
   * 
   * @param string String to be tested
   * @param defaultValue Value to return if string is null/zero-length
   * @return
   */
  public static String getValue(String string, String defaultValue)
  {
    return hasValue(string) ? string : defaultValue;
  }

}
