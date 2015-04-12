package za.co.discoverylife.st2git.git;

import java.io.File;
import java.io.IOException;


/**
 * 
 * @author Anton Schoultz - 2015
 *
 */
public class GitHelper
{
  File fGitRepoDir;// the .git folder
  String repositoryDir;
  String gitExecutable;
  int result;
  
  /*
Create a repo
  git init

Set up Users detail in the git repo.
  git config --global user.name "Anton Schoultz"
  git config --global user.email "AntonSc@discovery.co.za"
  
Create and switch to a branch
  git branch {branchname} 
  git checkout {branchname} --
  
Add all changed files to the index
  git add *.*
  
Commit indexed changes to the repo
  git commit
  */
  
  /**
   * Creates a helper for a GIT repository at the given folder.
   * Creates the repository if required.
   * @param fDir File object pointing to the root (or .git) folder
   * @throws Exception
   */
  public GitHelper(File fDir) throws Exception{
    if(!fDir.getName().equals(".git")){
      fDir = new File(fDir,".git");
    }
    fGitRepoDir=fDir;
    repositoryDir = fDir.getParentFile().getAbsolutePath();
    findExecutable(null);
    if (!isValidGitRepository()) {
      doGit("init");
    }
  }
  
  public static void main(String[] args)
  {
    try
    {
      GitHelper gh = new GitHelper(new File("D:/ST2GitRoot/TOOLS"));
      gh.createAndSelectBranch("master");
      gh.addAll("*.*");
      gh.commit("initial commit");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /** Create and select branch */
  public void createAndSelectBranch(String branchName){
    if(branchName.compareTo("master")!=0){
      createBranch(branchName);
    }
    selectBranch(branchName);
  }
  
  /** Create a remote host */
  public void addRemoteHost(GitHost host){
    doGit("remote","add",host.getRemoteName(),host.getRemoteUrl());
  }
  
  /** 
   * Pull from remote.
   * Fetches work from remote server's branch and merges with local.
   */
  public void pullFromRemote(String hostName, String branch){
    doGit("pull",hostName,branch);
  }
  
  /** 
   * Push updates to host.
   * Sends locally committed work up to the host server.
   */
  public void pushToRemote(String hostName, String branch){
    doGit("push",hostName,branch);
  }

  /** 
   * Select a branch to work with.
   * Sets work are up as per the branch specified.
   * @param branchName
   */
  public void selectBranch(String branchName)
  {
    doGit("checkout",branchName,"--");
  }

  private void createBranch(String branchName)
  {
    doGit("branch",branchName);
  }
  
  /** Add all files to index */
  public void addAll(String patern){
    doGit("add",patern);
  }
  
  /** Commit */
  public void commit(String remarks){
    doGit("commit","\"--message="+remarks+"\"");
  }
  
  private boolean isValidGitRepository() {
    return doGit("branch");
  }

  
  private boolean doGit(String... args) {
    ProcessBuilder process = new ProcessBuilder();
    int argc = args.length;
    String[] cmdArgs = new String[argc+1];
    cmdArgs[0]=gitExecutable;
    for(int i=0;i<argc;i++){
      cmdArgs[i+1]=args[i];
    }
    process.command(cmdArgs);
    process.directory(new File(repositoryDir));
    try {
      Process status = process.start();
      Thread statusOut = new Thread(new ErrorEater(status.getInputStream(), true));
      Thread statusErr = new Thread(new ErrorEater(status.getErrorStream(), cmdArgs[1]));
      statusOut.start();
      statusErr.start();
      result = status.waitFor();
      statusOut.join();
      statusErr.join();
      return (result == 0);
    } catch (IOException e) {
      System.out.println(e.getMessage());
    } catch (InterruptedException e) {
      System.out.println(e.getMessage());
    }
    return false;
  }

  
  private boolean findExecutable(String preferedPath) {
    String os = System.getProperty("os.name");
    if(null != preferedPath) {
      String fileExtension = "";
      if(os.contains("indow")) {
        fileExtension = ".exe";
      }
      File gitExec = new File(preferedPath + File.separator + "git" + fileExtension);
      if(canExecute(gitExec)) {
        try {
          gitExecutable = gitExec.getCanonicalPath();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    } else {
      if(os.contains("indow")) {
        File gitExec = new File("C:" + File.separator + "Program Files" + File.separator + 
            "Git" + File.separator + "bin" + File.separator + "git.exe");
        if(canExecute(gitExec)) {
          try {
            gitExecutable = gitExec.getCanonicalPath();
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          gitExec = new File("C:" + File.separator + "Program Files (x86)" + File.separator + 
              "Git" + File.separator + "bin" + File.separator + "git.exe");
          if(canExecute(gitExec)) {
            try {
              gitExecutable = gitExec.getCanonicalPath();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      } else {
        gitExecutable = "git";
      }
    }
    return (null != gitExecutable);
  }
  
  private boolean canExecute(File gitExec){
    //return gitExec.exists() && gitExec.canExecute();
    return gitExec.exists();
  }
}
