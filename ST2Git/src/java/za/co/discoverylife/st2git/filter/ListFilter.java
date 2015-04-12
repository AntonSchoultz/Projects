package za.co.discoverylife.st2git.filter;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import za.co.discoverylife.appcore.util.DateTime;
import za.co.discoverylife.st2git.Reference;
import za.co.discoverylife.st2git.ST2Git;
import za.co.discoverylife.st2git.util.ClassHelper;

public class ListFilter extends BaseFilter
{
  private static final String FILTER_PACKAGE = "za.co.discoverylife.st2git.filter";
  private ArrayList<IFilter> filters = new ArrayList<IFilter>();
  private TreeSet<String> skipped = new TreeSet<String>();
  private long cutOffDate = 0;
  private IFilter activeFilter;

  public void addAllFilters(String... repos)
  {
    try
    {
      Set<Class<?>> listClasses = ClassHelper.findClasses(FILTER_PACKAGE);
      for (Class<?> filterClass : listClasses)
      {
        String name = filterClass.getSimpleName();
        if (name.startsWith("Filter"))
        {
          // Found a filter
          IFilter filter = (IFilter) filterClass.newInstance();
          if (isIn(filter.getRepoName(), repos))
          {
            // only add if the repo it works with matches one in the list
            System.out.println("Added filter " + name);
            addFilter(filter);
          }
        }
      }
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public boolean include(String projectName)
  {
    for (IFilter filter : filters)
    {
      if (filter.include(projectName))
      {
        activeFilter = filter;
        return true;
      }
    }
    skipped.add(projectName);
    return false;
  }

  public boolean check(Reference reference)
  {
    boolean process = false;
    for (IFilter filter : filters)
    {
      if (filter.include(reference.stProject))
      {
        if (filter.check(reference))
        {
          process = true;
          break;
        }
      }
    }
    if (reference.timestamp < cutOffDate)
    {
      // System.out.println("\t\t\t\tToo Old "+reference.toString());
      process = false;
      reference.repo = null;// too old, so de-select
    }
    return (process);
  }

  public void addFilter(String name)
  {
    String className = FILTER_PACKAGE + ".Filter" + name;
    try
    {
      IFilter aFilter = (IFilter) Class.forName(className).newInstance();
      filters.add(aFilter);
    }
    catch (Exception e)
    {
      ST2Git.error("ERROR creating filter " + className, e);
    }
  }

  public void addFilter(IFilter aFilter)
  {
    filters.add(aFilter);
  }

  /** Set cut off date as 'yyyy/mm/dd' */
  public void setCutOffDate(String ymd)
  {
    DateTime dtm = new DateTime(ymd);
    cutOffDate = dtm.getTimeInMillis();
  }

  public TreeSet<String> getSkipped()
  {
    return skipped;
  }

  public IFilter getActiveFilter()
  {
    return activeFilter;
  }

  public String getRepoName()
  {
    return null;
  }
}
