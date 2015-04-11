package za.co.discoverylife.appcore;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Properties;

import za.co.discoverylife.appcore.logging.FileLogger;
import za.co.discoverylife.appcore.logging.ILogListener;
import za.co.discoverylife.appcore.logging.ILogger;
import za.co.discoverylife.appcore.logging.LogManager;
import za.co.discoverylife.appcore.plugin.ModuleList;
import za.co.discoverylife.appcore.shell.AntShell;
import za.co.discoverylife.appcore.shell.JavaShell;
import za.co.discoverylife.appcore.task.ITaskConstants;
import za.co.discoverylife.appcore.task.TaskManager;
import za.co.discoverylife.appcore.util.PropertyUtil;
import za.co.discoverylife.appcore.util.XmlUtil;

/**
 * Provides an application with self-discovery and automatic library jars.
 * 
 * @author anton11
 */
public abstract class ApplicationBase implements ITaskConstants, Serializable
{
  private static final String PLUG_IN_DIR = "plugins";

  private static final long serialVersionUID = -6089416792782245439L;

  protected static ApplicationBase appBase;

  /** List of plug-in modules */
  protected ModuleList moduleList;

  protected transient LogManager logManager;
  protected transient String appName;
  protected transient boolean isJar = false;
  protected transient File fAppHome;
  protected transient File fLib;
  protected transient File fJavaHome;
  protected transient String userName;
  protected transient ILogger log;
  protected transient File workDir;
  protected transient boolean shutdown = false;
  protected transient TaskManager taskManager = TaskManager.getInstance();

  /**
   * Protected constructor - establishes application home folder. - sets user
   * name - initializes log manager with a console output - sets up a shutdown
   * hook
   */
  protected ApplicationBase()
  {
    System.out
        .println("######################################################################################################");
    PropertyUtil.echoSystemProperties();
    String currentdir = System.getProperty("user.dir");
    findJDK();
    workDir = new File(currentdir);
    appName = this.getClass().getSimpleName();
    logManager = LogManager.getInstance();
    log = LogManager.getLogger(this.getClass());
    log.info("Starting up");
    findApplicationHome();
    // user name
    userName = System.getProperty("user.name");
    log.info("Registering shutdown hook");
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
    {
      public void run()
      {
        Thread.currentThread().setName("SHUTDOWN");
        initShutDown();
      }
    }));
    appBase = this;
    // initialise plug in list
    if ( moduleList == null )
    {
      moduleList = new ModuleList();
    }
    DataHolder.store(moduleList);
  }

  /**
   * Ensures that moduleList is set-up, scans the plugins dir for any new
   * plug-in jars which are then added to the list of available modules.
   */
  public void updateAvailablePluginModules()
  {
    if ( moduleList == null )
    {
      moduleList = new ModuleList();
    }
    DataHolder.store(moduleList);
    moduleList.updateAvailableModules();
    moduleList.loadActivePlugins();
  }

  /**
   * Returns a String which lists all system properties (for debug purposes)
   */
  public static String getSystemPropertiesAsString()
  {
    StringBuffer sb = new StringBuffer();
    Properties pSys = System.getProperties();
    for (Object o : pSys.keySet())
    {
      String key = (String) o;
      String value = pSys.getProperty(key);
      sb.append(key).append("='").append(value).append("'").append("\r\n");
    }
    return sb.toString();
  }

  /** Adds a command line logger */
  protected void addCommandLineLogger()
  {
    logManager.addCommandLineLogger();
  }

  /** Attempt to get the implementation version for the object provided */
  public String getVersion(Object obj)
  {
    String ver = obj.getClass().getPackage().getImplementationVersion();
    return ver;
  }

  /**
   * Adds a file logger to log manager's output
   */
  public void addFileLogger()
  {
    ILogListener fileLog = new FileLogger(getApplicationFile(appName + ".log"));
    logManager.addLogListener(fileLog);
  }

  /** Return the application base */
  public static ApplicationBase getApplicationBase()
  {
    return appBase;
  }

  /** User class must implement the shutdown actions to call storeState() etc */
  protected void shutdown()
  {
    System.out.println("AppBase.shutdown()-calling taskman waitUntilDone(2)");
    TaskManager.getInstance().waitDone(1);
    System.out.println("AppBase.shutdown()-done");
  }

  /**
   * Shutdown hook, calls user shutdown() method
   */
  protected void initShutDown()
  {
    shutdown = true;
    System.out.println("AppBase:initShutDown():started");
    log.clear();
    shutdown();
    logManager.close();
    System.out.println("JVM shutdown complete");
  }

  /**
   * Recalls the application state object
   * 
   * @param obj
   */
  protected void recallState(Object obj)
  {
    try
    {
      String name = obj.getClass().getSimpleName();
      File fp = getApplicationFile(name + ".xml");
      log.info("Recalling state information " + name);
      new XmlUtil().recallObject(obj, fp);
    }
    catch (Exception e)
    {
      log.error("Error recalling state for " + obj, e);
      System.err.println("Error recalling state for " + obj);
      e.printStackTrace(System.err);
    }
  }

  /**
   * Stores the application state object
   * 
   * @param obj
   */
  protected void storeState(Object obj)
  {
    if ( obj == null )
      return;
    try
    {
      String name = obj.getClass().getSimpleName();
      File fp = getApplicationFile(name + ".xml");
      log.debug("Storing state information " + name);
      new XmlUtil().storeObject(obj, fp);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Locates where the application is running from
   */
  private void findApplicationHome()
  {
    // First, try to find where this class was loaded from
    String key = this.getClass().getName().replace(".", "/") + ".class";
    ClassLoader.getSystemClassLoader();
    URL url = ClassLoader.getSystemResource(key);
    fAppHome = null;
    if ( url == null )
    {
      // if that doesn't work then perhaps we were packaged by 'OneJar'
      // so try to find that ...
      url = ClassLoader.getSystemResource("OneJar.class");
    }
    if ( url == null )
    {
      // still not found, maybe running as classes (eg from eclipse)
      // so try to create a file and use that
      File fX = new File("x");
      fAppHome = fX.getParentFile();
      if ( fAppHome == null )
      {
        System.err.println("Could not find parent file for File('x') " + fX.getAbsolutePath());
      }
      log.warn("Could not establish application's home: defaulting to " + fAppHome.getAbsolutePath());
    }
    else
    {
      String spec = url.toString();
      if ( spec.startsWith("jar:") )
      {
        // 'jar:file:dist/MyAppCore-0.0.0.jar!/za/co/discoverylife/appcore/Application.class'
        isJar = true;
        int bx = spec.lastIndexOf(':');
        int ex = spec.indexOf('!');
        fAppHome = new File(spec.substring(bx + 1, ex)).getParentFile();
      }
      else
      {
        // 'file:/home/anton11/workspace/MyAppCore/bin/za/co/discoverylife/appcore/Application.class'
        isJar = false;
        int bx = spec.lastIndexOf(':');
        int ex = spec.indexOf(key);
        fAppHome = new File(spec.substring(bx + 1, ex - 1));
      }
      log.info("Application Home:" + fAppHome.getAbsolutePath());
    }
    if ( fAppHome.getName().equalsIgnoreCase("dist") )
    {
      fAppHome = fAppHome.getParentFile();
    }
    if ( fAppHome.getName().equalsIgnoreCase("bin") )
    {
      fAppHome = fAppHome.getParentFile();
    }
    if ( fAppHome.getName().equalsIgnoreCase("build") )
    {
      fAppHome = fAppHome.getParentFile();
    }
    fLib = new File(fAppHome, "lib");
    System.out.println("ApplicationHome=" + fAppHome);
  }

  /** Returns the File object which points to the application's home */
  public File getApplicationHome()
  {
    return fAppHome;
  }

  //	/** checks for a library sub directory */
  //	private boolean hasLibDir() {
  //		fLib = new File(fAppHome, "lib");
  //		if (!fLib.exists() || !fLib.isDirectory()) {
  //			fLib = null;
  //			return false;
  //		}
  //		return true;
  //	}

  /**
   * if we have a lib folder, go add it's jars to the class path
   */
  protected void addLibJars()
  {
    if ( fLib != null )
    {
      addJars(fLib);
    }
  }

  /**
   * Rolls over jars renaming them to $xxxx.jar for the given folder (if folder
   * exists)
   * 
   * This allows the application to update it's own jars (as the renamed
   * versions are the ones that are locked.)
   * 
   * @param jarDir
   *          Folder to rename jars in
   */
  protected void rollOverJars(File jarDir)
  {
    System.out.println("Rolling over jars in " + jarDir.getAbsolutePath());
    if ( jarDir != null && jarDir.exists() && jarDir.isDirectory() )
    {
      ArrayList<File> jars = new ArrayList<File>();
      String name;
      String lname;
      // list all $*.jar
      for (File f : jarDir.listFiles())
      {
        name = f.getName().toLowerCase();
        lname = name.toLowerCase();
        if ( !lname.endsWith(".jar") && !lname.endsWith(".zip") )
        {
          continue;// skip if not .jar or .zip
        }
        if ( !f.getName().startsWith("$") )
        {
          jars.add(f);
        }
      }
      // now roll over
      for (File f : jars)
      {
        File $f = new File(jarDir, "$" + f.getName());
        if ( $f.exists() )
        {
          $f.delete();
        }
        f.renameTo($f);
      }
    }
  }

  /**
   * Adds all the jar files in the provided folder to the classpath (provided
   * that the folder exists)
   * 
   * @param libDir
   */
  public void addJars(File libDir)
  {
    if ( libDir != null && libDir.exists() && libDir.isDirectory() )
    {
      for (File f : libDir.listFiles())
      {
        if ( f.getName().toLowerCase().endsWith(".jar") )
        {
          try
          {
            addURL(f.toURI().toURL());
          }
          catch (IOException e)
          {
            System.err.println("Error adding jar to classpath " + f.getName() + ":" + e.getMessage());
          }
        }
      }
    }
  }

  /**
   * Add file specification to class loader path
   * 
   * @param jarUrl
   *          URL specifying a jar file or a directory which contains classes to
   *          be added
   * @throws IOException
   */
  @SuppressWarnings("rawtypes")
  protected void addURL(URL jarUrl) throws IOException
  {
    final Class[] parameters = new Class[]{URL.class};
    URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class sysclass = URLClassLoader.class;
    try
    {
      Method method = sysclass.getDeclaredMethod("addURL", parameters);
      method.setAccessible(true);
      method.invoke(sysloader, new Object[]{jarUrl});
      System.out.println("ApplicationBase:Added classloader URL '" + jarUrl + "'");
    }
    catch (Throwable t)
    {
      t.printStackTrace();
      throw new IOException("Error, could not add URL to system classloader " + jarUrl.toString());
    }// end try catch
  }// end method

  /** try to establish the JDK's home folder */
  protected void findJDK()
  {
    String javaHome = System.getProperty("java.home");
    if ( javaHome != null )
    {
      fJavaHome = new File(javaHome);
      if ( !tryJDK(fJavaHome) )
      {
        if ( !tryJDK(fJavaHome.getParentFile()) )
        {
          for (File f : fJavaHome.getParentFile().listFiles())
          {
            if ( tryJDK(f) )
              break;
          }
        }
      }
      System.out.println("JavaHome:" + fJavaHome.getAbsolutePath());
      JavaShell.setJdkHome(fJavaHome.getAbsolutePath());
      try
      {
        AntShell.setAntHome(fJavaHome.getAbsolutePath());
      }
      catch (Exception e)
      {
      }
    }
  }

  // helper method to find JDK
  private boolean tryJDK(File f)
  {
    if ( !f.isDirectory() )
      return false;
    boolean flg = f.getName().toLowerCase().startsWith("jdk");
    if ( flg )
    {
      fJavaHome = f;
    }
    return flg;
  }

  /**
   * Return a file specified relative to the application home. If name is null,
   * returns appHome. AppHome is the directory that the application is running
   * from.
   * 
   * @param fileName
   * @return File object that points to the requested file.
   */
  public File getApplicationFile(String fileName)
  {
    if ( fileName == null || fileName.trim().length() == 0 )
      return fAppHome;
    return new File(fAppHome, fileName);
  }

  /** Returns the user's name */
  public String getUser()
  {
    return userName;
  }

  /**
   * Return a file specified relative to the user's home/work folder. 
   * If name is null, returns the user's home/work folder. 
   * 
   * @param fileName
   * @return File object that points to the requested file.
   */
  public File getUserFile(String fileName)
  {
    if ( fileName == null || fileName.trim().length() == 0 )
    {
      return workDir;
    }
    return new File(workDir, fileName);
  }

  /** Returns application's lib folder */
  public File getLibFolder()
  {
    return fLib;
  }

  public File getPluginsFolder()
  {
    return getApplicationFile(PLUG_IN_DIR);
  }

}
