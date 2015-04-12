/**
 * Issue.java
 */
package za.co.discoverylife.st2git.starteam;



/**
 * Holds information about a source control system issue
 * 
 * @author Anton Schoultz (2013)
 */
public class Issue implements Comparable<Issue>
{
  public static final int ISSUE_NOTE = 0;
  public static final int ISSUE_FORCED_FOLDER = 1;
  public static final int ISSUE_LABEL_VIEW = 2;
  public static final int ISSUE_LABELED_JAR = 3;
  public static final int ISSUE_JAVA_COMPLIANCE = 4;

  public static final String[] ISSUE_NAME = {
      "Note",
      "Forced Folder",
      "Label/View",
      "Labeled JAR",
      "JavaCompliance"
  };

  /** Issue type code */
  private int type;

  /** Additional description for the issue */
  private String description;

  /**
   * Null CONSTRUCTOR required for persistence
   */
  public Issue()
  {
  }

  /**
   * CONSTRUCTOR which accepts the issue code and an additional description.
   * 
   * @param type Issue type code (ISSUE_*)
   * @param description Additional information about the issue
   */
  public Issue(int type, String description)
  {
    super();
    this.type = type;
    this.description = description;
  }

  /** Returns IssueTypeName:description */
  public String toString()
  {
    return ISSUE_NAME[type] + ":" + description;
  }

  /**
   * @param type type to check for
   * @return true if it matches the required type
   */
  public boolean isType(int type)
  {
    return this.type == type;
  }

  /** Returns the name of the issue type  */
  public String getTypeName()
  {
    if ( type < 0 || type > ISSUE_NAME.length )
    {
      return "Undefined Issue";
    }
    return ISSUE_NAME[type];
  }

  /** This comparison allows sorting by type, description */
  public int compareTo(Issue o)
  {
    int delta = type - o.type;
    if ( delta == 0 )
    {
      delta = description.compareTo(o.description);
    }
    return delta;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public int getType()
  {
    return type;
  }

  public void setType(int type)
  {
    this.type = type;
  }

}
