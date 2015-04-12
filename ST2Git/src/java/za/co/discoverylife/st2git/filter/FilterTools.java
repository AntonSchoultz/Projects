package za.co.discoverylife.st2git.filter;

import za.co.discoverylife.st2git.Reference;

public class FilterTools extends BaseFilter
{
  private static final String[] PROJECTS = new String[] {
      "Bob",
      //"Tools",
      /// AntExtensions
      /// BobBulkScriptTools, BobCodeReview, BobScheduler, BobSQL
      /// CodeChecker, DataChecker, DSIQuoteGenerator
      /// Icons, InsertRevisionAnnotation
      /// SystemTool, WebLogicMonitor
  };
  private static final String[] LABELS = new String[] {
      "LATEST_STABLE", "RELEASE"
  };

  public boolean check(Reference reference)
  {
    include = fixTimeStamp(reference);
    include |= isIn(reference.stLabel, LABELS);
    // if included, setup convert details
    if (include)
    {
      reference.repo = getRepoName();
      reference.key = reference.stView + "#" + reference.stLabel;
      reference.gitBranch = "master";
      reference.gitLabel = reference.stLabel;
    }
    return include;
  }

  public String getRepoName()
  {
    return TOOLS;
  }

  public boolean include(String projectName)
  {
    return isIn(projectName, PROJECTS);
  }
}
