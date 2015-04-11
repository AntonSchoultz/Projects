package za.co.discoverylife.appcore.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import za.co.discoverylife.appcore.logging.ILogger;
import za.co.discoverylife.appcore.task.BaseTask;

/**
 * Provides the basic functionality to call a command line process, handling it's STDOUT, STDERR
 * etc.
 * 
 * @author Anton Schoultz - 21 Jan 2010
 */
public abstract class BaseShell extends BaseTask
{
  protected static String FILE_SEPARATOR = File.separator;
  protected static String PATH_SEPARATOR = File.pathSeparator;
  public static int OS_UNKNOWN = 0;
  public static int OS_WINDOWS = 1;
  public static int OS_UNIX = 2;

  protected String refName = "";
  protected ILogger log = null;

  private static final int MAX_HISTORY = 10;
  protected String[] lastErrLines = new String[MAX_HISTORY];

  protected Map<String, String> envir;
  protected List<String> args;
  protected StringBuilder sbClassPath;
  protected int operatingSystemId = 0;

  protected String YMD;
  protected String hms;

  private Process process;
  StreamCopy cpyOut = null;
  StreamCopy cpyErr = null;

  protected Map<String, String> mapParams;

  /** Controls the way logs are written, true=append, false=new file */
  public boolean append = true;

  public boolean gotResponse = false;

  // SYSTEM PROPERTIES - some useful ones ? :)
  // java.home = "C:\Program Files\Java\jre1.5.0"
  // sun.boot.library.path = "C:\Program Files\Java\jre1.5.0\bin"
  // user.home=d:\Users Documents\anton11
  // user.name=anton11
  // sun.cpu.endian=little

  /** CONSTRUCT base shell handler */
  public BaseShell(String reference)
  {
    this.refName = reference == null ? "" : reference;
    envir = new TreeMap<String, String>();
    args = new ArrayList<String>();
    mapParams = new TreeMap<String, String>();
    sbClassPath = new StringBuilder();
    log = null;
    String os = System.getProperty("os.name").toLowerCase();
    if ( os.indexOf("windows") >= 0 )
    {
      operatingSystemId = OS_WINDOWS;
    }
    if ( os.indexOf("unix") >= 0 )
    {
      operatingSystemId = OS_UNIX;
    }
  }

  /** Returns true if Windows operating system was detected */
  public boolean isWindows()
  {
    return operatingSystemId == OS_WINDOWS;
  }

  /** Returns true if UNIX operating system was detected */
  public boolean isUnix()
  {
    return operatingSystemId == OS_UNIX;
  }

  /**
   * Adds a single argument string to the command, if it contains spaces, the argument will be
   * enclosed in quotes.
   * 
   * @param arg
   */
  public void addArgument(String arg)
  {
    args.add(quoteSpaces(arg));
  }

  /**
   * Adds '-optionId optionValue'
   * 
   * @param optionId
   *          option name
   * @param optionValue
   *          value (will be enclosed in quotes if it has spaces)
   */
  public void addOption(String optionId, String optionValue)
  {
    args.add("-" + optionId);
    if ( optionValue != null )
    {
      addArgument(optionValue);
    }
  }

  /**
   * Adds '/flag[:value]'
   * 
   * @param flag
   * @param value
   */
  public void addFlag(String flag, String value)
  {
    if ( value != null )
    {
      addArgument("/" + flag.trim() + ":" + value.trim());
    }
    else
    {
      addArgument("/" + flag.trim());
    }
  }

  /** Adds command line parameter */
  public void setParam(String key, String value)
  {
    mapParams.put(key, value);
  }

  /** Adds the provided properties as cmd line parameters '-Dname=value' */
  public void addParams(Properties props)
  {
    for (Entry<Object, Object> entry : props.entrySet())
    {
      String var = entry.getKey().toString();
      String val = entry.getValue().toString();
      setParam(var, val);
    }
  }

  /** Add the provided folder to the class path string */
  public void addClassPath(File folder)
  {
    addClassPath(folder.getAbsolutePath());
  }

  /** Add the provided folder name to the class path string */
  public void addClassPath(String itemToAdd)
  {
    File fPath = new File(itemToAdd);
    if ( sbClassPath.length() > 0 )
    {
      sbClassPath.append(PATH_SEPARATOR);
    }
    sbClassPath.append(quoteSpaces(fPath.getAbsolutePath()));
  }

  /** Add an environment parameter to pass to command line */
  public void addEnvParameter(String name, String value)
  {
    envir.put(name, value);
  }

  /** returns environment as Sting[] of 'VARNAME=VALUE' entries */
  public String[] getEnvironment()
  {
    List<String> envList = new ArrayList<String>();
    for (String k : envir.keySet())
    {
      String v = envir.get(k);
      envList.add(k + "=" + v);
      log.info("ENVIRONMENT:- " + k + "=" + v);
    }
    String[] envAry = envList.toArray(new String[1]);
    return envAry;
  }

  /** Sets up time stamp values for YMD and hms */
  public void setTimeStamps()
  {
    SimpleDateFormat sdfYMD = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat sdfHMS = new SimpleDateFormat("HHmmss");
    Date dte = new Date();
    YMD = sdfYMD.format(dte);
    hms = sdfHMS.format(dte);
  }

  /**
   * Shell to execute the provided command, routing all STDOUT to the out file, and STDERR to the
   * err file. output is logged as debug messages, while error
   * output is logged as error messages
   * 
   * @param cmd
   *          command line to shell
   * @param out
   *          {@link File} to send STDOUT to
   * @param err
   *          {@link File} to send ERROUT to
   * @return int return code 0=OK
   */
  public int execute(String cmd, File out, File err)
  {
    // log.info("SHELL> " + formatCmdLine(cmd));
    // log.info("SHELL> " + cmd);
    Runtime rt = Runtime.getRuntime();
    int rc = 0;
    try
    {
      gotResponse = false;
      PrintWriter pwOut = null;//new PrintWriter(out);
      if ( out.getName().startsWith("Job_") )
      {
        pwOut = new PrintWriter(out);
      }
      log.debug("starting cmd shell");
      // process = rt.exec(cmd, getEnvironment());
      log.info("INHERITING ENVIRONMENT");
      process = rt.exec(cmd, null);// inherit environment
      log.debug("hooking into cmd shell stdout and stderr...");
      // hook into STDOUT, STDERR
      cpyOut = new StreamCopy(process.getInputStream(), pwOut, "out");
      cpyErr = new StreamCopy(process.getErrorStream(), pwOut, "err");
      Thread thOut = new Thread(cpyOut);
      thOut.setName(Thread.currentThread().getName() + "-sysout");
      thOut.start();
      Thread thErr = new Thread(cpyErr);
      thErr.setName(Thread.currentThread().getName() + "-syserr");
      thErr.start();
      // wait for process
      // log.debug("Waiting for shell cmd to exit.");
      rc = process.waitFor();
    }
    catch (InterruptedException ei)
    {
      rc = -1;
      log.error("INTERRUPTED: " + cmd, ei);
    }
    catch (Exception e)
    {
      rc = -2;
      log.warn("Error executing: " + cmd);
      e.printStackTrace();
    }
    log.debug("cmd shell has exited, busy flushing stdout and stderr");
    int n = 5;
    while (cpyOut != null && cpyOut.busy && cpyErr != null && cpyErr.busy && n > 0)
    {
      try
      {
        Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
        break;
      }
      n--;
    }
    log.info("JavaShell: rc=" + rc);
    if ( !gotResponse )
    {
      log.warn("No response from command line for " + cmd.toString());
    }
    return rc;
  }

  /** Formats command line string for display purposes */
  protected String formatCmdLine(String cmd)
  {
    StringBuilder sb = new StringBuilder();
    boolean blank = false;
    for (char c : cmd.toCharArray())
    {
      if ( c == File.pathSeparatorChar )
      {
        c = ':';
      }
      switch (c)
      {
        case ' ' :
          blank = true;
          sb.append(c);
          break;
        case '-' :
          if ( blank )
          {
            sb.append("\r\n -");
          }
          else
          {
            sb.append(c);
          }
          blank = false;
          break;
        case ':' :
          sb.append("\r\n\t").append(File.pathSeparator);
          blank = false;
          break;
        default :
          sb.append(c);
          blank = false;
          break;
      }
    }
    sb.append("\r\n");
    return sb.toString();
  }

  /** Halt/destroy the process being run */
  public void halt()
  {
    log.error("*** Halting process **** ");
    terminateShell();
  }

  /** Halt/destroy the process being run */
  public void cancel()
  {
    log.error("*** Canceling process **** ");
    terminateShell();
  }

  /** Kill the shell cmd */
  private void terminateShell()
  {
    if ( process != null )
    {
      cpyOut.stop();
      cpyErr.stop();
      process.destroy();
      process = null;
    }
  }

  /** if the provided string contains spaces, then wrap it in quotes */
  public String quoteSpaces(String input)
  {
    input = input.trim();
    if ( input.indexOf(" ") < 0 )
      return input;
    return "\"" + input + "\"";
  }

  /**
   * @return the lastErrLine
   */
  public String getLastErrLine()
  {
    return lastErrLines[MAX_HISTORY - 1];
  }

  public String[] getLastErrLines()
  {
    return lastErrLines;
  }

  /**
   * @param lastErrLine
   *          the lastErrLine to set
   */
  public void setLastErrLine(String lastErrLine)
  {
    int mx = MAX_HISTORY - 1;
    for (int i = 0; i < mx; i++)
    {
      lastErrLines[i] = lastErrLines[i + 1];
    }
    lastErrLines[mx] = lastErrLine;
  }

  /**
   * Echos STDOUT/STDERR lines to log system
   * 
   * @param type
   * @param line
   */
  private synchronized void echo(String type, String line)
  {
    if ( type.equalsIgnoreCase("err") )
    {
      log.warn(refName + ":" + line);
    }
    else
    {
      log.debug(refName + ":" + line);
    }
  }

  public ILogger getLog()
  {
    return log;
  }

  public void setLog(ILogger log)
  {
    this.log = log;
  }

  /**
   * Helper thread class to read process STDOUT and STDERR to prevent stalling. info is echoed via
   * echo method
   * 
   * @author Anton Schoultz - 19 Jan 2010
   */
  private class StreamCopy implements Runnable
  {
    String type;
    boolean busy = false;

    boolean mayRun = true;

    InputStreamReader isr;
    BufferedReader br;
    PrintWriter pw;

    /**
     * CONSTRUCT a stream handler
     * 
     * @param is
     *          stream to be read
     * @param outFile
     *          file to write it to
     * @param type
     *          type of stream 'err' or 'out'
     * @throws FileNotFoundException
     */
    private StreamCopy(InputStream is, PrintWriter outWriter, String type) throws FileNotFoundException
    {
      this.type = type;
      isr = new InputStreamReader(is);
      br = new BufferedReader(isr);
      pw = outWriter;
    }

    public void stop()
    {
      mayRun = false;
    }

    public void println(String msg)
    {
      if ( pw != null )
      {
        pw.println(msg);
      }
      gotResponse = true;
    }

    /** read the stream, echo to log, and write to file - until EOF */
    public void run()
    {
      busy = true;
      try
      {
        String line = null;
        while (((line = br.readLine()) != null) && mayRun)
        {
          println(line);
          echo(type, line);
        }
        if ( pw != null )
        {
          pw.flush();
          pw.close();
        }
        isr.close();
      }
      catch (IOException ioe)
      {
        ioe.printStackTrace();
      }
      busy = false;
    }
  }

}
