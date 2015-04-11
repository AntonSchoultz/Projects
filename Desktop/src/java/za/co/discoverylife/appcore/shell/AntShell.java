package za.co.discoverylife.appcore.shell;

import java.io.File;

import za.co.discoverylife.appcore.logging.LogManager;

/**
 * Provides a shell to execute ANT builds.
 * 
 * @author anton11
 *
 */
public class AntShell extends JavaShell

{
  private static File fAntHome;
  private static File fAntLauncherJar;

  protected File fAntBuildXml;
  protected String antTarget = "build";
  protected File fLog;

  /**
   * CONSTRUCT shell task to launch ANT builder
   * 
   * @param fAntBuildXml
   *          File which points to the ant script to be called
   * @param target
   *          String name of ant target to execute (may be null, then not passed and ant will use default in the script)
   */
  public AntShell(File fAntBuildXml, String target)
  {
    super();
    this.fAntBuildXml = fAntBuildXml;
    this.antTarget = target;
    log = LogManager.getLogger(getClass());
    this.taskName = "ANT(" + fAntBuildXml.getParentFile().getName() + "/" + fAntBuildXml.getName() + " @ " + target
        + ")";
    // System.out.println("## AntShell ## " + taskName);
  }

  /** Sets the home folder where ANT is installed */
  public static void setAntHome(String antHome) throws Exception
  {
    fAntHome = new File(antHome);
    if ( !fAntHome.isDirectory() || !fAntHome.exists() )
    {
      throw new Exception("Ant home is invalid '" + fAntHome.getAbsolutePath() + "'");
    }
    fAntLauncherJar = new File(fAntHome, "lib" + FILE_SEPARATOR + "ant-launcher.jar");
    if ( !fAntLauncherJar.exists() )
    {
      throw new Exception("Could not find an ant-launcher '" + fAntLauncherJar.getAbsolutePath() + "'");
    }
  }

  /** Returns ANT home */
  public static String getAntHome()
  {
    return fAntHome == null ? null : fAntHome.getAbsolutePath();
  }

  /*
   * (non-Javadoc)
   * @see za.co.discoverylife.bob.cmd.task.ITask#performTask(java.lang.String)
   */
  /** executes the ANT build */
  public void executeTask()
  {
    if ( fAntLauncherJar == null )
    {
      fail(new Exception("Ant not set up correctly yet"));
    }
    if ( !fAntBuildXml.exists() )
    {
      fail(new Exception("Ant build script not found " + fAntBuildXml.getAbsolutePath()));
    }
    try
    {
      setState(STATE_ACTIVE);// mark as active & start timer
      addEnvParameter("CLASSPATH", jreBin);
      addEnvParameter("JAVA_HOME", jdkHome);
      addEnvParameter("ANT_HOME", getAntHome());
      addClassPath(fAntLauncherJar);
      addJdkLib("tools.jar");
      addArgument("-Dant.home=" + getAntHome());
      addArgument("-Djava.home=" + jreHome);
      addArgument("org.apache.tools.ant.launch.Launcher");
      addOption("f", fAntBuildXml.getAbsolutePath());
      if ( antTarget != null )
      {
        addArgument(antTarget);
      }
      File fBase = fAntBuildXml.getParentFile();
      String antName = fAntBuildXml.getName();
      int ix = antName.indexOf(".");
      fLog = new File(fBase, antName.substring(0, ix) + ".log");
      File fErr = fLog;// new File(fBase, antName + ".err.log");
      String cmdLine = getCmdString();
      log.debug("EXEC-CMD>" + cmdLine);
      int rc = execute(cmdLine, fLog, fErr);
      if ( rc != 0 )
      {
        log.info("Java call to ANT for build Script " + fAntBuildXml.getAbsolutePath() + " failed with rc=" + rc
            + " - see log for details:" + fLog.getAbsolutePath());
        fail(new Exception("ANT failed while trying to execute target '" + antTarget + "' in script "
            + fAntBuildXml.getAbsolutePath()));
      }
      else
      {
        setState(STATE_DONE);// mark as done, stop timer
      }
    }
    catch (Exception e)
    {
      fail(new Exception("Problem executing ant shell ", e));
      e.printStackTrace();
    }
  }

  public File getLogFile()
  {
    return fLog;
  }

}
