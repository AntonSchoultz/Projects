package za.co.discoverylife.appcore.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Implements ILogListener to Log to file.
 * 
 * @author anton11
 */
public class FileLogger
    implements ILogListener
{
  private File logFile;
  private PrintWriter pw = null;
  private FileWriter fos = null;

  private int logLevel = 0;
  private SimpleDateFormat date_time = new SimpleDateFormat("yyyyMMdd-HHmmss");
  // private SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss ");

  private boolean appendMode = false;
  private boolean keep = true;

  /** if true, only log the selected level */
  ;
  private boolean selectMode = false;

  /** 
   * CONSTRUCT a file logger to write to the provided file 
   * 
   * @param logFile File to write to
   * @param appendMode True to append to the file (false=new file)
   */
  public FileLogger(File logFile, boolean appendMode)
  {
    setLogFile(logFile);
    this.appendMode = appendMode;
  }

  /** CONSTRUCT a file logger to write to the provided file */
  public FileLogger(File logFile)
  {
    setLogFile(logFile);
    this.appendMode = false;
  }

  public void setLogLevel(int level)
  {
    logLevel = level;
  }

  /**
   * Implements ILogListener.doLog(~) to send message to the file
   */
  public void doLog(int lvl, String msg, Exception e)
  {
    if ( isSelectMode() && (lvl != logLevel) )
    {
      return;
    }
    if ( lvl >= logLevel )
    {
      checkOpen();
      println(LOG_PREFIX[lvl] + "\t" + msg);
      if ( e != null )
      {
        Throwable t = e;
        while (t != null)
        {
          println("    :  " + t.getMessage());
          t = t.getCause();
        }
      }
      pw.flush();
    }
  }

  /**
   * Implements ILogListener to clear by closing the file.
   * Any subsequent message will cause a new file to be started.
   */
  public void clear()
  {
    close();
    keep = false;
  }

  private void println(String msg)
  {
    checkOpen();
    // pw.println(time.format(new Date()) + " [" + Thread.currentThread().getName() + "] " + msg);
    pw.println(msg);
  }

  /**
   * Open a file for writing the log to.
   */
  public void checkOpen()
  {
    if ( pw == null )
    {
      try
      {
        fos = new FileWriter(logFile, appendMode && keep && logFile.exists());
        pw = new PrintWriter(fos);
        pw.println("\r\n: Logging (re)started at " + date_time.format(new Date()) + " for "
            + Thread.currentThread().getName() + "\r\n:");
      }
      catch (FileNotFoundException e)
      {
        e.printStackTrace();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }

  /** Flush and close the log file */
  public void close()
  {
    if ( pw != null )
    {
      // pw.println("-- Log file closed at " + sdf.format(new Date()));
      pw.flush();
      pw.close();
      try
      {
        fos.close();
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
    pw = null;
    fos = null;
  }

  public File getLogFile()
  {
    return logFile;
  }

  private void setLogFile(File logFile)
  {
    this.logFile = logFile;
    File dir = logFile.getParentFile();
    if ( !dir.exists() )
    {
      dir.mkdirs();
    }
  }

  public boolean isSelectMode()
  {
    return selectMode;
  }

  public void setSelectMode(boolean selectMode)
  {
    this.selectMode = selectMode;
  }

}
