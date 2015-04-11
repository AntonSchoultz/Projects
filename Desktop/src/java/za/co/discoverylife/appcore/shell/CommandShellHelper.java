package za.co.discoverylife.appcore.shell;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Helper to execute command line commands
 * 
 * @author anton11
 */
public class CommandShellHelper
    extends BaseShell
{

  protected File workingDir = null;
  protected File scriptFile = null;
  protected PrintWriter pwScript = null;

  /**
   * CONSTRUCTS a new command helper.
   * 
   * @param workingDir
   *          {@link File} that will be used as Cur.Work.Dir
   */
  public CommandShellHelper(File workingDir) {
    super(workingDir.getName());
    this.workingDir = workingDir;
  }

  /**
   * CONSTRUCTS a new command helper to create a script.
   * 
   * @param workingDir
   *          {@link File} that will be used as Cur.Work.Dir
   * @param scriptName
   *          Name for the script file to produce (eg bld.bat)
   * @throws FileNotFoundException
   */
  public CommandShellHelper(File workingDir, String scriptName)
      throws FileNotFoundException {
    this(workingDir);
    startScript(scriptName);
  }

  /** Adds a simple text line to the script - eg remarks */
  public CommandShellHelper addScriptLine(String line)
  {
    flush();
    if (pwScript != null) {
      pwScript.println(getCmdString());
    }
    return this;
  }

  /**
   * Starts a new command.
   * (If there was a previous command, and script is open, then
   * the previous command will be written to the script first)
   * 
   * @param cmdName
   *          CommandName eg 'javac'
   */
  public CommandShellHelper startCommand(String cmdName)
  {
    flush();
    args = new ArrayList<String>();
    args.add(cmdName);
    return this;
  }

  /** Returns the effective command line */
  public String getCmdString()
  {
    StringBuilder sb = new StringBuilder();
    for (String s : args) {
      if (sb.length() > 0) {
        sb.append(' ');
      }
      sb.append(s);
    }
    return sb.toString();
  }

  /**
   * Starts a script to write commands to
   * 
   * @param scriptName
   * @throws FileNotFoundException
   */
  public CommandShellHelper startScript(String scriptName) throws FileNotFoundException
  {
    closeScript();
    scriptFile = new File(workingDir, scriptName);
    pwScript = new PrintWriter(scriptFile);
    return this;
  }

  /**
   * Close the script file.
   */
  public CommandShellHelper closeScript()
  {
    if (pwScript != null) {
      pwScript.flush();
      pwScript.close();
      pwScript = null;
    }
    return this;
  }

  /**
   * Writes this command to the script
   */
  public CommandShellHelper flush()
  {
    if (args.size() > 0 && pwScript != null) {
      pwScript.println(getCmdString());
    }
    return this;
  }

  /**
   * Execute the script that has been generated.
   * 
   * @return int return code 0=OK
   */
	public void executeTask()
  {
    File err = new File(scriptFile.getAbsoluteFile() + "-err.log");
    File out = new File(scriptFile.getAbsoluteFile() + "-out.log");
    String scriptName = scriptFile.getAbsolutePath();
		execute(scriptName, out, err);
  }
}
