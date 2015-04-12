package za.co.discoverylife.st2git.convert;

import java.util.ArrayList;
import za.co.discoverylife.st2git.Reference;

public class ConvertRepository
{
  public String repo;
  private String brkKey="";
  
  private ArrayList<ConvertTask> tasks;
  ConvertTask cvtTask;

  public ConvertRepository(String repo)
  {
    super();
    this.repo = repo;
    tasks = new ArrayList<ConvertTask>();
    brkKey="";
  }
  
  public void addReference(Reference ref){
    // reject if not same repo
    if(ref.repo.compareTo(repo)!=0) return;
    // check if we have a task for this key yet
    if(ref.key.compareTo(brkKey)!=0){
      brkKey=ref.key;
      String comment = ref.getYmdHms()+" "+ref.stLabel+" "+ref.stLabelDesrcription; 
      cvtTask = new ConvertTask(repo, brkKey,comment);
      tasks.add(cvtTask);
    }
    cvtTask.addReference(ref);
  }

  public ConvertTask findTask(String keyName){
    for(ConvertTask cvtTask:tasks){
      if(cvtTask.gitKey.compareTo(keyName)==0){
        return cvtTask;
      }
    }
    return null;
  }

  
  public ArrayList<ConvertTask> getTasks()
  {
    return tasks;
  }

  public String getRepo()
  {
    return repo;
  }
  
  
  public void execute() throws Exception
  {
    for(ConvertTask cvtTask:tasks){
      cvtTask.execute();
    }
  }

  public void toXml(StringBuilder sb, String pad){
    sb.append(pad).append("<Repository gitRepo='").append(repo).append("'>\r\n");
    for(ConvertTask cvtTask:tasks){
      cvtTask.toXml(sb,pad+"    ");
    }
    sb.append(pad).append("</Repository>").append("\r\n");
  }
  
  
}
