package za.co.discoverylife.st2git.filter;

import za.co.discoverylife.appcore.util.DateTime;
import za.co.discoverylife.st2git.Reference;

/**
 * Provides some basic helper methods for Filters.
 *  
 * @author Anton Schoultz - 2015
 */
public abstract class BaseFilter implements IFilter
{
  public static final String DELPHI = "DELPHI";
  public static final String LIFE = "LIFE";
  public static final String TOOLS = "TOOLS";

  public boolean include;

  /** Default CONSTRUCTOR */
  public BaseFilter()
  {
    super();
  }
  
  /**
   * Tests to see if the value matches one of the strings in the list
   * @param value value to look for
   * @param list array of string to check against
   * @return true if found
   */
  protected boolean isIn(String value, String[] list){
    for(String s:list){
      if(s.compareTo(value)==0){
        return true;
      }
    }
    return false;
  }
  
  /**
   * Checks the label description to see if it was created by Bob
   * while creating an integration label. If so, this corrects the label
   * time stamp to the time Bob created the label.
   * @param reference
   * @return true if it was an integration label and time was fixed.
   */
  protected boolean fixTimeStamp(Reference reference){
    if (reference.stLabelDesrcription.startsWith("LATEST_STABLE as at "))
    {
      String ymdhms = reference.stLabelDesrcription.substring(20, 39);
      DateTime dtm = new DateTime(ymdhms);
      reference.setTimestamp(dtm.getTimeInMillis());
      return true;
    }
    return false;
  }
  
  
}
