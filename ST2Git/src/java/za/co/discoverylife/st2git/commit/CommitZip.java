package za.co.discoverylife.st2git.commit;

import java.io.File;
import za.co.discoverylife.st2git.ST2Git;
import za.co.discoverylife.st2git.util.ZipHelper;

public class CommitZip implements ICommitter
{
  File fRoot;
  File fZipRoot;
  
  public CommitZip(File root){
    fRoot = ST2Git.getWorkingRoot();
    fZipRoot = new File(fRoot,"ZIP");
    fZipRoot.mkdirs();
  }

  public void commit(String repository,String branch, String label,String comment)
  {
    try
    {
      File fRepoRoot = new File(fRoot,repository);
      fRepoRoot.mkdirs();
      File fZipRepo = new File(fZipRoot,repository);
      fZipRepo.mkdirs();
      String zipName = branch+"#"+label;
      zipName = zipName.replace('/', '$').replace('\\', '$');
      System.out.print("Creating ZIP "+zipName+" ... ");
      File fZip = new File(fZipRepo, zipName+".zip");
      ZipHelper zh = new ZipHelper(fZip);
      zh.createZip(fRepoRoot);
      System.out.println(" Done.");
    }
    catch (Exception e)
    {
      String msg = "Error committing as a ZIP (repository="
          +repository+", branch="+branch+", label="+label+")";
      ST2Git.error(msg,e);
    }
  }
}
