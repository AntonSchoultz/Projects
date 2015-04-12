package za.co.discoverylife.st2git.commit;

public interface ICommitter
{
  public void commit(String repository, String branch,String label,String comment);
}
