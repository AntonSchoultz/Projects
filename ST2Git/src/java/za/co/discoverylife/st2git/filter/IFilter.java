package za.co.discoverylife.st2git.filter;

import za.co.discoverylife.st2git.Reference;

public interface IFilter
{
  public boolean include(String projectName);
  public boolean check(Reference reference);
  public String getRepoName();
}
