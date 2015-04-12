package za.co.discoverylife.st2git.starteam;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/** 
 * Holds information about the most recent check-out including
 * <ul>
 * <li>a collection of review information items</li>
 * <li>a list of check-out issues</li>
 * <li>a list of recent modifiers</li>
 * </ul>
 * 
 * @author Anton Schoultz
 */
public class Result 
{
  private static final long OLD_DAYS = 7;
  private static final long MS_PER_DAY = 24 * 60 * 60 * 1000;

  /** Name of the project that these results are for */
  private String projectName;

  /** The last build label that was used for the checkout */
  private String label;

  /** The root folder for this project on the local machine */
  private String rootFolder;

  /** Starting label for the change report */
  private String fromLabel;

  /** Ending label for the change report */
  private String toLabel;

  /** List of result items */
  private ArrayList<ResultItem> resultItemlist;

  /** List of issues detected during the check-out */
  private ArrayList<Issue> issueList;

  /** Count of the number of files expected/fetched */
  private int fileCount = 0;

  private int recent = 0;

  /** Current time in msec */
  private long nowTime = System.currentTimeMillis();

  /** time stamp 'OLD_DAYS' ago */
  private long oldTime = nowTime - (MS_PER_DAY * OLD_DAYS);

  /** ID code of the reviewer */
  private int reviewerID = 0;

  /** Track modifiers and counts */
  protected transient HashMap<String, UserCount> modifierMap;

  /** CONSTRUCTOR */
  public Result(String projectName, String label)
  {
    this.projectName = projectName;
    this.label = label;
    resultItemlist = new ArrayList<ResultItem>();
    issueList = new ArrayList<Issue>();
    modifierMap = new HashMap<String, UserCount>();
  }

  /**
   * @return String of the current date and time as yyyy/MM/dd HH:mm
   */
  public String getTimeStamp()
  {
    DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    return df.format(new Date(nowTime));
  }

  /** 
   * Returns message with date range for 'recent' items as 
   * ' (Recent is from yyyy/mm/dd hh:mm to yyyy/mm/dd hh:mm)'
   */
  public String getDateRangeString()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(" (Recent is from ");
    Date oldDte = new Date();
    oldDte.setTime(oldTime);
    Date nowDte = new Date();
    nowDte.setTime(nowTime);
    DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    sb.append(df.format(oldDte));
    sb.append(" to ");
    sb.append(df.format(nowDte));
    sb.append(") ");
    return sb.toString();
  }

  /**
   * Append the contents of the provided reviewInfo into this reviewInfo 
   * @param reviewInfo ReviewInfo object whose contents are to be added
   */
  public void append(Result reviewInfo)
  {
    for (ResultItem item : reviewInfo.resultItemlist)
    {
      addItem(item);
    }
  }

  /**
   * Remove all matching items and return what's left.
   * @param result Other results (to be removed)
   * @return List<ResultItem> remaining items.
   */
  public List<ResultItem> removeAllItems(Result result)
  {
    for (ResultItem item : result.resultItemlist)
    {
      removeItem(item);
    }
    return resultItemlist;
  }

  /**
   * Removes the specified item from the list (matched by host reference of path+filename)
   * @param item The ResultItem to be removed
   * @return true if found and removed.
   */
  public boolean removeItem(ResultItem item)
  {
    String key = item.getHostReference();
    ResultItem zapItem = findByHostReference(key);
    if ( zapItem != null )
    {
      resultItemlist.remove(zapItem);
      return true;
    }
    return false;
  }

  /** 
   * Adds a review info item to the list 
   * 
   * @param item ResultItem to be added
   * <br>key is fileSpec/fileName
   */
  public void addItem(ResultItem item)
  {
    if ( !resultItemlist.contains(item) )
    {
      resultItemlist.add(item);
      incrementModifierCount(item.getModifierName());
    }
  }

  /** Adds a list of items to the review items and updates counters etc */
  public void addItems(List<ResultItem> itemList)
  {
    if ( itemList != null )
    {
      for (ResultItem rii : itemList)
      {
        addItem(rii);
      }
    }
  }

  /** Adds an issue to the list of issues found */
  public void addIssue(int type, String description)
  {
    Issue issue = new Issue(type, description);
    issueList.add(issue);
  }

  /** Returns number of review items in the list */
  public int size()
  {
    return resultItemlist.size();
  }

  public ResultItem findByHostReference(String key)
  {
    for (ResultItem ri : resultItemlist)
    {
      if ( key.equalsIgnoreCase(ri.getHostReference()) )
      {
        return ri;
      }
    }
    return null;
  }

  /** 
   * Returns a copy of the list, sorted according to the supplied comparator
   * <p>eg {@link ResultItemByDate}, {@link ResultItemByModifierFile}
   * 
   * @param comparator
   * @return
   */
  public ArrayList<ResultItem> getSortedList(Comparator<ResultItem> comparator)
  {
    return getSortedList(comparator, resultItemlist);
  }

  /** 
   * Returns a copy of the list, sorted according to the supplied comparator
   * <p>eg {@link ResultItemByDate}, {@link ResultItemByModifierFile}
   * 
   * @param comparator
   * @return
   */
  public ArrayList<ResultItem> getSortedList(Comparator<ResultItem> comparator, ArrayList<ResultItem> lst)
  {
    ArrayList<ResultItem> sorted = new ArrayList<ResultItem>();
    sorted.addAll(lst);
    Collections.sort(sorted, comparator);
    return sorted;
  }

  /** Returns ALL code review items */
  public ArrayList<ResultItem> getList()
  {
    return resultItemlist;
  }

  /** Returns the list of items which require code review */
  public ArrayList<ResultItem> getReviewRequiredList()
  {
    ArrayList<ResultItem> lst = getListByReviewState(ResultItem.REVIEW_REQUIRED);
    // filter to only include .java files
    ArrayList<ResultItem> codeList = new ArrayList<ResultItem>();
    for (ResultItem item : lst)
    {
      if ( item.getFileName().toLowerCase().endsWith(".java") )
      {
        codeList.add(item);
      }
    }
    return codeList;
  }

  /** 
   * Returns a list of review item which match the provided state
   * 
   * (State constants defined in {@link ResultItem})
   *
   * @param state
   * @return
   */
  public ArrayList<ResultItem> getListByState(int state)
  {
    ArrayList<ResultItem> lst = new ArrayList<ResultItem>();
    for (ResultItem item : resultItemlist)
    {
      if ( item.isState(state) )
      {
        lst.add(item);
      }
    }
    Collections.sort(lst);
    return lst;
  }

  /** 
   * Returns a list of (.java) review item which match the provided review state
   * <p>
   * <b>NOTE:</b> Only .java files are considered for this list.
   * <p>
   * (State constants defined in {@link ResultItem})
   *
   * @param state
   * @return
   */
  public ArrayList<ResultItem> getListByReviewState(int state)
  {
    ArrayList<ResultItem> lst = new ArrayList<ResultItem>();
    for (ResultItem item : resultItemlist)
    {
      if ( state == item.getReviewCd() )
      {
        if ( item.getFileName().toLowerCase().endsWith(".java") )
        {
          lst.add(item);
        }
      }
    }
    Collections.sort(lst);
    return lst;
  }

  public String getProjectName()
  {
    return projectName;
  }

  /** 
   * Returns a summary list of all item as a string report.
   * @return
   */
  public String reportSummary()
  {
    StringBuilder sb = new StringBuilder();
    for (ResultItem item : resultItemlist)
    {
      sb.append(item.toShortString());
      sb.append("\r\n");
    }
    return sb.toString();
  }

  public int getReviewerID()
  {
    return reviewerID;
  }

  public void setReviewerID(int reviewerID)
  {
    this.reviewerID = reviewerID;
  }

  public long getOldTime()
  {
    return oldTime;
  }

  public void setOldTime(long oldTime)
  {
    this.oldTime = oldTime;
  }

  public String getRootFolder()
  {
    return rootFolder;
  }

  public void setRootFolder(String rootFolder)
  {
    this.rootFolder = rootFolder;
  }

  public String getFromLabel()
  {
    return fromLabel;
  }

  public void setFromLabel(String fromLabel)
  {
    this.fromLabel = fromLabel;
  }

  public String getToLabel()
  {
    return toLabel;
  }

  public void setToLabel(String toLabel)
  {
    this.toLabel = toLabel;
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
  }

  @Override
  public String toString()
  {
    return "Result [projectName=" + projectName + ", label=" + label + ", rootFolder=" + rootFolder + ", fromLabel="
        + fromLabel + ", toLabel=" + toLabel
        //+ ", passed=" + passed + ", failed=" + failed + ", todo=" + todo
        + ", recent=" + recent + ", nowTime=" + nowTime + ", oldTime=" + oldTime + ", reviewerID=" + reviewerID + "]";
  }

  public int getRecent()
  {
    return recent;
  }

  public void setRecent(int recent)
  {
    this.recent = recent;
  }

  public int getFileCount()
  {
    return fileCount;
  }

  public void setFileCount(int fileCount)
  {
    this.fileCount = fileCount;
  }

  /** Update modification counts for the given user name */
  public void incrementModifierCount(String userName)
  {
    UserCount count = modifierMap.get(userName);
    if ( count == null )
    {
      count = new UserCount(userName);
      modifierMap.put(userName, count);
    }
    count.increment();
  }

  /** 
   * Sort the most frequent modifiers and return the list of names.
   * <p>The list is sorted in descending order of the number of modifications. 
   * @return String[] of modifier user names
   */
  public String[] obtainModifierList()
  {
    String[] sa = new String[modifierMap.size()];
    ArrayList<UserCount> ary = new ArrayList<UserCount>();
    ary.addAll(modifierMap.values());
    Collections.sort(ary, new SortUserByCount());
    int n = 0;
    for (UserCount uc : ary)
    {
      sa[n++] = uc.getName();
    }
    return sa;
  }

  /** Tracks number of modifications per user */
  class UserCount
  {
    private String name;
    private int count = 0;
    UserCount(String name)
    {
      this.name = name;
    }
    public void increment()
    {
      count++;
    }
    public int getCount()
    {
      return count;
    }
    public String getName()
    {
      return name;
    }
    public String toString()
    {
      return count + "\t" + name;
    }
  }

  /** Used to sort UserCount by count */
  class SortUserByCount
      implements Comparator<UserCount>
  {
    public int compare(UserCount i0, UserCount i1)
    {
      int t0 = i0.getCount();
      int t1 = i1.getCount();
      return t1 - t0;
    }
  }

  /**
   * Returns true if any issues were detected
   */
  public boolean hasIssues()
  {
    return issueList.size() > 0;
  }

  /** Returns the list of issues detected */
  public ArrayList<Issue> getIssueList()
  {
    return issueList;
  }

  /** Returns the list of specified issues */
  public ArrayList<Issue> getIssueList(int type)
  {
    ArrayList<Issue> list = new ArrayList<Issue>();
    for (Issue i : issueList)
    {
      if ( i.isType(type) )
      {
        list.add(i);
      }
    }
    return list;
  }

}
