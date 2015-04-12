package za.co.discoverylife.st2git.git;

public class GitHost
{
  private String remoteName;
  private String remoteUrl;
  

  public GitHost()
  {
    // TODO Auto-generated constructor stub
  }
 
  public GitHost(String remoteName, String remoteUrl)
  {
    this();
    this.remoteName = remoteName;
    this.remoteUrl = remoteUrl;
  }

  public String getRemoteName()
  {
    return remoteName;
  }

  public void setRemoteName(String remoteName)
  {
    this.remoteName = remoteName;
  }

  public String getRemoteUrl()
  {
    return remoteUrl;
  }

  public void setRemoteUrl(String remoteUrl)
  {
    this.remoteUrl = remoteUrl;
  }
  
  
}
