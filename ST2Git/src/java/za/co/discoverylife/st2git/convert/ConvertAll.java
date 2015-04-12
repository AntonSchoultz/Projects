package za.co.discoverylife.st2git.convert;

import java.util.ArrayList;
import za.co.discoverylife.st2git.Reference;

public class ConvertAll
{
  ArrayList<ConvertRepository> cvtRepoList = new ArrayList<ConvertRepository>();
  ConvertRepository cvtRepo;
  
  public void addReference(Reference ref){
    if(cvtRepo==null){
      cvtRepo = new ConvertRepository(ref.repo);
      cvtRepoList.add(cvtRepo);
    }
    if(cvtRepo.repo.compareTo(ref.repo)!=0){
      cvtRepo = new ConvertRepository(ref.repo);
      cvtRepoList.add(cvtRepo);
    }
    cvtRepo.addReference(ref);
  }

  public ArrayList<ConvertRepository> getConvertRepositoryList()
  {
    return cvtRepoList;
  }
  
  public ConvertRepository findRepo(String repoName){
    for(ConvertRepository cvtRepo:cvtRepoList){
      if(cvtRepo.repo.compareTo(repoName)==0){
        return cvtRepo;
      }
    }
    return null;
  }
  
  public void execute() throws Exception
  {
    for(ConvertRepository cvtRepo:cvtRepoList){
      cvtRepo.execute();
    }
  }
  
  public void toXml(StringBuilder sb, String pad){
    sb.append(pad).append("<Convert>").append("\r\n");
    for(ConvertRepository cvtRepo:cvtRepoList){
      cvtRepo.toXml(sb,pad+"    ");
    }
    sb.append(pad).append("</Convert>").append("\r\n");
  }

}
