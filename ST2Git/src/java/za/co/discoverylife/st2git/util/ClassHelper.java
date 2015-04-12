package za.co.discoverylife.st2git.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Utility to get a list of all classes in a package (and sub-packages)
 * 
 * Obtained from a discussion group.
 */
public final class ClassHelper
{
  /**
   * Returns a list of all classes within the given package name.
   */
  public static Set<Class<?>> findClasses(String packageName) throws Exception
  {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    return findClasses(loader, packageName);
  }

  /**
   * Returns a list of all classes within the given package name,
   * as resolved by the provided ClassLoader.
   */
  public static Set<Class<?>> findClasses(ClassLoader loader, String packageName) throws Exception
  {
    Set<Class<?>> classes = new HashSet<Class<?>>();
    String path = packageName.replace('.', '/');
    Enumeration<URL> resources = loader.getResources(path);
    if (resources != null)
    {
      while (resources.hasMoreElements())
      {
        String filePath = resources.nextElement().getFile();
        if (filePath != null)
        {
          // WINDOWS HACK
          if (filePath.indexOf("%20") > 0)
            filePath = filePath.replaceAll("%20", " ");
          if ((filePath.indexOf("!") > 0) & (filePath.indexOf(".jar") > 0))
          {
            String jarPath = filePath.substring(0, filePath.indexOf("!"))
                .substring(filePath.indexOf(":") + 1);
            // WINDOWS HACK
            if (jarPath.indexOf(":") >= 0)
              jarPath = jarPath.substring(1);
            classes.addAll(findClassesInJar(jarPath, path));
          }
          else
          {
            classes.addAll(findClassesInDirectory(new File(filePath), packageName));
          }
        }
      }
    }
    return classes;
  }

  /**
   * Returns a set of classes found within a directory that match the provided package.
   */
  public static Set<Class<?>> findClassesInDirectory(File directory, String packageName)
      throws Exception
  {
    Set<Class<?>> classes = new HashSet<Class<?>>();
    if (directory.exists())
    {
      for (File f : directory.listFiles())
      {
        if (f.isFile() && f.getName().endsWith(".class"))
        {
          String name = packageName + '.' + stripFilenameExtension(f.getName());
          try
          {
            Class<?> clazz = Class.forName(name);
            classes.add(clazz);
          }
          catch (Exception e)
          {
          }
        }
        if (f.isDirectory())
        {
          classes.addAll(findClassesInDirectory(f, packageName + "." + f.getName()));
        }
      }
    }
    return classes;
  }

  /**
   * Returns a set of classes found within a jar that match the provided package
   */
  public static Set<Class<?>> findClassesInJar(String jar, String packageName) throws Exception
  {
    Set<Class<?>> classes = new HashSet<Class<?>>();
    JarInputStream jarFile = new JarInputStream(new FileInputStream(jar));
    JarEntry jarEntry;
    do
    {
      jarEntry = jarFile.getNextJarEntry();
      if (jarEntry != null)
      {
        String className = jarEntry.getName();
        if (className.endsWith(".class"))
        {
          className = stripFilenameExtension(className);
          if (className.startsWith(packageName))
            classes.add(Class.forName(className.replace('/', '.')));
        }
      }
    }
    while (jarEntry != null);
    jarFile.close();
    return classes;
  }

  /**
   * Returns the filename without the trailing extension.
   */
  public static String stripFilenameExtension(String className)
  {
    int ix = className.lastIndexOf('.');
    if (ix > 0)
    {
      return className.substring(0, ix);
    }
    return className;
  }

  /**
   * Locates the root of the current application, returning it's folder as a File object
   * 
   * @return File object pointing to the root of the application
   */
  public static File findApplicationHome()
  {
    // First, try to find where this class was loaded from
    String key = ClassHelper.class.getName().replace(".", "/") + ".class";
    ClassLoader.getSystemClassLoader();
    URL url = ClassLoader.getSystemResource(key);
    File fAppHome = null;
    if (url == null)
    {
      // if that doesn't work then perhaps we were packaged by 'OneJar'
      // so try to find that ...
      url = ClassLoader.getSystemResource("OneJar.class");
    }
    if (url == null)
    {
      // still not found, maybe running as classes (eg from eclipse)
      // so try to create a file and use that
      File fX = new File("x");
      fAppHome = fX.getParentFile();
      if (fAppHome == null)
      {
        System.err.println("Could not find parent file for File('x') " + fX.getAbsolutePath());
      }
      // System.out.println("Could not establish application's home: defaulting to " +
      // fAppHome.getAbsolutePath());
    }
    else
    {
      String spec = url.toString();
      if (spec.startsWith("jar:"))
      {
        int bx = spec.lastIndexOf(':');
        int ex = spec.indexOf('!');
        fAppHome = new File(spec.substring(bx + 1, ex)).getParentFile();
      }
      else
      {
        int bx = spec.lastIndexOf(':');
        int ex = spec.indexOf(key);
        fAppHome = new File(spec.substring(bx + 1, ex - 1));
      }
    }
    if (fAppHome.getName().equalsIgnoreCase("dist"))
    {
      fAppHome = fAppHome.getParentFile();
    }
    if (fAppHome.getName().equalsIgnoreCase("bin"))
    {
      fAppHome = fAppHome.getParentFile();
    }
    if (fAppHome.getName().equalsIgnoreCase("build"))
    {
      fAppHome = fAppHome.getParentFile();
    }
    return fAppHome;
  }

  /**
   * Renames all jar and zip files in the provider folder to $xxxxxx.jar/zip
   * 
   * @param jarDir
   *          Folder to rename jars in
   */
  public static void rollOverJars(File jarDir)
  {
    if (jarDir != null && jarDir.exists() && jarDir.isDirectory())
    {
      ArrayList<File> jars = new ArrayList<File>();
      String name;
      String lname;
      // list all $*.jar
      for (File f : jarDir.listFiles())
      {
        name = f.getName().toLowerCase();
        lname = name.toLowerCase();
        if (!lname.endsWith(".jar") && !lname.endsWith(".zip"))
        {
          continue;// skip if not .jar or .zip
        }
        if (!f.getName().startsWith("$"))
        {
          jars.add(f);
        }
      }
      // now roll over
      for (File f : jars)
      {
        File $f = new File(jarDir, "$" + f.getName());
        if ($f.exists())
        {
          $f.delete();
        }
        f.renameTo($f);
      }
    }
  }

  /**
   * Adds all the jar files in the provided folder to the class path
   * 
   * @throws Exception
   */
  public static void pathAddLib(File libDir) throws Exception
  {
    if (libDir != null && libDir.exists() && libDir.isDirectory())
    {
      for (File f : libDir.listFiles())
      {
        if (f.getName().toLowerCase().endsWith(".jar"))
        {
          try
          {
            pathAddJar(f);
          }
          catch (IOException e)
          {
            throw new Exception("Error adding jar to classpath " + f.getName() + ":"
                + e.getMessage());
          }
        }
      }
    }
  }

  /**
   * Adds the specified jar file to the class path.
   */
  public static void pathAddJar(File fJarFile) throws Exception
  {
    if (fJarFile.exists() && fJarFile.getName().toLowerCase().endsWith(".jar"))
    {
      pathAddURL(fJarFile.toURI().toURL());
    }
  }

  /**
   * Add the jar, specified by the given URL, to the class path.
   * 
   * @param jarUrl
   * @throws IOException
   */
  public static void pathAddURL(URL jarUrl) throws Exception
  {
    final Class<?>[] parameters = new Class[] { URL.class };
    URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<?> sysclass = URLClassLoader.class;
    try
    {
      Method method = sysclass.getDeclaredMethod("addURL", parameters);
      method.setAccessible(true);
      method.invoke(sysloader, new Object[] { jarUrl });
    }
    catch (Throwable t)
    {
      t.printStackTrace();
      throw new IOException("Error, could not add URL to system classloader " + jarUrl.toString());
    }// end try catch
  }// end method

  /**
   * Returns the location of the JDK (if possible, else throws exception)
   */
  public static File findJDK() throws Exception
  {
    String javaHome = System.getProperty("java.home");
    if (javaHome == null)
    {
      throw new Exception("java.home is not set");
    }
    File fJDK = new File(javaHome);
    if (tryJDK(fJDK))
    {
      return fJDK;
    }
    fJDK = fJDK.getParentFile();
    if (tryJDK(fJDK))
    {
      return fJDK;
    }
    for (File f : fJDK.getParentFile().listFiles())
    {
      if (tryJDK(f))
        return f;
    }
    throw new Exception("Could not find JDK from java.home="+javaHome);
  }

  /** returns true if f is a folder that starts with 'jdk' */
  public static boolean tryJDK(File f)
  {
    if (!f.isDirectory())
      return false;
    boolean flg = f.getName().toLowerCase().startsWith("jdk");
    return flg;
  }
}
