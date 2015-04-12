package za.co.discoverylife.st2git.commit;

import java.io.File;
import za.co.discoverylife.st2git.ST2Git;
import za.co.discoverylife.st2git.git.GitHelper;

public class CommitGit implements ICommitter
{
  File fRoot;
  
  public CommitGit(File root){
    fRoot = ST2Git.getWorkingRoot();
  }

  public void commit(String repository,String branch, String label,String comment)
  {
    try
    {
      // the root folder to be committed
      File fRepoRoot = new File(fRoot,repository);
      //
      System.out.println("### Commit contents of "+fRepoRoot.getAbsolutePath()
          +"\r\n#\tto repository "+repository
          +"\r\n#\t\tbranch "+branch
          +"\r\n#\t\t\tlabel "+label
          +"\r\n###"
          );

      GitHelper git = new GitHelper(fRepoRoot);
      git.createAndSelectBranch(branch);
      git.addAll("*.*");
      git.commit(comment);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
