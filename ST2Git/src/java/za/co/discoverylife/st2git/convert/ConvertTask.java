package za.co.discoverylife.st2git.convert;

import java.io.File;
import java.util.ArrayList;
import za.co.discoverylife.appcore.util.Convert;
import za.co.discoverylife.st2git.Reference;
import za.co.discoverylife.st2git.ST2Git;
import za.co.discoverylife.st2git.host.ServerSpecification;
import za.co.discoverylife.st2git.starteam.StarteamClient2005;

public class ConvertTask
{
  public String gitRepo;
  public String gitKey;
  
  private ArrayList<Reference> refs;
  private String comment;
  
  public ConvertTask(String gitRepo, String gitKey,String comment)
  {
    super();
    this.gitRepo = gitRepo;
    this.gitKey = gitKey;
    refs = new ArrayList<Reference>();
    this.comment = comment;
  }
  
  /** 
   * Add a reference provided that it matches the repository and key values.
   * @param ref
   */
  public void addReference(Reference ref){
    // reject if the reference is not for this repository
    if(ref.repo.compareTo(gitRepo)!=0) return;
    // reject if the key does not match this job
    if(ref.key.compareTo(gitKey)!=0) return;
    // OK
    refs.add(ref);
  }

  public ArrayList<Reference> getRefs()
  {
    return refs;
  }
  
  public void execute() throws Exception
  {
    long begin = System.currentTimeMillis();
    File fRoot = new File(ST2Git.getWorkingRoot(),gitRepo);
    ServerSpecification stHost = ST2Git.getStarteamHost();
    StarteamClient2005 client = new StarteamClient2005(stHost);
    // Check-out
    for(Reference ref:refs){
      try
      {
        System.out.println("#### Setup to checkOut for :"+ref.toString());
        String projectName = ref.stProject;
        File fLocal = new File(fRoot,projectName);
        String sLocalProjectRoot = fLocal.getAbsolutePath();
        client.setProjectRoot(sLocalProjectRoot, projectName);
        client.$select(projectName, ref.stView, ref.stLabel);
        System.out.println("#### CheckOut for :"+ref.toString());
        client.checkOut();
      }
      catch (Exception e)
      {
        ST2Git.error("ERROR checking out "+ref.toString(),e);
      }
    }
    client.disconnect();
    // commit
    Reference ref = refs.get(0);
    String label = ref.gitLabel;
    System.out.println("Compleated checkout in " + Convert.mSecToHMS(System.currentTimeMillis()-begin) );
    ST2Git.getCommiter().commit(gitRepo, ref.gitBranch, label, comment);
    System.out.println("Compleated checkout and commit in " + Convert.mSecToHMS(System.currentTimeMillis()-begin) );
  }

  
  public void toXml(StringBuilder sb, String pad){
    sb.append(pad).append("<Task gitKey='").append(gitKey).append("'>\r\n");
    for(Reference ref:refs){
      ref.toXml(sb,pad+"    ");
    }
    sb.append(pad).append("</Task>").append("\r\n");
  }
  
}
