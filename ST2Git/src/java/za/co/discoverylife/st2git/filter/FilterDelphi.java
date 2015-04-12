package za.co.discoverylife.st2git.filter;

import za.co.discoverylife.st2git.Reference;

public class FilterDelphi extends BaseFilter
{

  public boolean check(Reference reference)
  {
    include=true;// all included
    // setup for conversion
    if(include){
      reference.repo=getRepoName();
      reference.key=reference.stView +"#"+reference.stLabel;
      reference.gitBranch = reference.stView;
      reference.gitLabel = reference.stLabel;
    }
    return include;
  }

  public boolean include(String projectName)
  {
    return (projectName.compareTo("NB_Delphi_STI")==0);
    //return projectName.startsWith("NB_Delphi_");
  }

  public String getRepoName()
  {
    return DELPHI;
  }
}
