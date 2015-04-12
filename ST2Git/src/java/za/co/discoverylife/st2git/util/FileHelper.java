package za.co.discoverylife.st2git.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

/**
 * File utilities
 * <dl>
 * <dt>delTree
 * <dd>Delete all files and folders from the provided one downwards.
 * <dt>fileCopy
 * <dd>Copy a file from source to target using NIO
 * <dt>fileCopyIfNewer
 * <dd>Copy a file from source to target using NIO (only if source is newer)
 * <dt>fileWrite
 * <dd>Writes the supplied string to the specified file.
 * <dt>fileRead
 * <dd>Reads the specified text file as a string
 * <dt>fileReadLines
 * <dd>Reads the file and returns it's contents as a List of lines
 * </dl>
 * 
 * @author Anton Schoultz - 2010
 */
public class FileHelper
{

  /** Touch a file (set last modified time) */
  public static boolean touchFile(File file)
  {
    return touchFile(file, System.currentTimeMillis());
  }

  /** Renames a file/folder to the upper-case version */
  public static File toUpper(File f)
  {
    String name = f.getName();
    String tgtName = name.toUpperCase();
    if ( tgtName.compareTo(name) == 0 )
    {
      return f;
    }
    File ftmp = new File(f.getParentFile(), "tmp_" + name);
    File fnew = new File(f.getParentFile(), tgtName);
    f.renameTo(ftmp);
    if ( fnew.exists() )
    {
      fnew.delete();
    }
    ftmp.renameTo(fnew);
    return fnew;
  }

  /** Renames a file/folder to the lower-case version */
  public static File toLower(File f)
  {
    String name = f.getName();
    String tgtName = name.toLowerCase();
    if ( tgtName.compareTo(name) == 0 )
    {
      return f;
    }
    File ftmp = new File(f.getParentFile(), "tmp_" + name);
    File fnew = new File(f.getParentFile(), tgtName);
    f.renameTo(ftmp);
    ftmp.renameTo(fnew);
    return fnew;
  }

  /** Touch a file (set last modified time to the provided timestamp) */
  public static boolean touchFile(File file, long timestamp)
  {
    if ( file.exists() && file.canWrite() )
    {
      return file.setLastModified(timestamp);
    }
    return false;
  }

  /** 
   * Returns extension of a file in lower case (excluding the period)
   * 
   * @param f File object to get the extension for
   * @return String extension (or null if directory or no ext)
   */
  public static String getExt(File f)
  {
    if ( f == null || !f.isFile() )
      return null;
    String name = f.getName() + " ";
    int ix = name.lastIndexOf(".");
    if ( ix < 0 )
    {
      return null;
    }
    return name.substring(ix + 1).trim();
  }

  /**
   * Fixes the slashes used in a string specification to the current OS version
   */
  public static String fixSlashes(String spec)
  {
    File f = new File(spec.replace("\\", File.separator).replace("/", File.separator));
    return f.getAbsolutePath();
  }

  /** Return the number of files in a directory and it's sub-folders */
  public static long getFileCount(File dir)
  {
    long count = 0;
    if ( !dir.exists() )
    {
      return 0;
    }
    for (File f : dir.listFiles())
    {
      if ( f.isDirectory() )
      {
        count = count + getFileCount(f);
      }
      else
      {
        count++;
      }
    }
    return count;
  }

  /**
   * Delete all files and folders from the provided one downwards.
   * 
   * @param dir
   * @return
   */
  public static long delTree(File dir)
  {
    long n = 0;
    if ( dir == null || !dir.isDirectory() || !dir.exists() )
    {
      return 0;
    }
    if ( !dir.canWrite() )
    {
      return 0;
    }
    for (File f : dir.listFiles())
    {
      if ( f.isDirectory() )
      {
        n += delTree(f);
      }
      else
      {
        f.delete();
        n++;
      }
    }
    dir.delete();
    return n;
  }

  /** Copy a file from source to target using NIO (only if source is newer) */
  public static long fileCopyIfNewer(File source, File target)
  {
    if ( source.lastModified() > target.lastModified() )
    {
      return fileCopy(source, target);
    }
    return source.length();
  }

  /** Copy a file from source to target using NIO */
  public static long fileCopy(File source, File target)
  {
    long size = -1;
    FileChannel src = null;
    FileChannel tgt = null;
    try
    {
      src = new FileInputStream(source).getChannel();
      tgt = new FileOutputStream(target).getChannel();
      size = src.size();
      tgt.transferFrom(src, 0, size);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      if ( src != null )
      {
        try
        {
          src.close();
        }
        catch (IOException e)
        {
        }
      }
      if ( tgt != null )
      {
        try
        {
          tgt.close();
        }
        catch (IOException e)
        {
        }
      }
    }
    return size;
  }

  /** 
   * Copy a whole directory
   * 
   * @param srcDir Source folder to copy from
   * @param tgtDir Target folder to copy to
   * @param recurse true to recurse into sub-folders
   * @return number of files copied
   */
  public static int dirCopy(File srcDir, File tgtDir, boolean recurse)
  {
    int n = 0;
    if ( !srcDir.exists() || !srcDir.isDirectory() )
      return 0;
    if ( !tgtDir.exists() )
    {
      tgtDir.mkdirs();
    }
    for (File fSrc : srcDir.listFiles())
    {
      File fTgt = new File(tgtDir, fSrc.getName());
      if ( fSrc.isFile() )
      {
        fileCopy(fSrc, fTgt);
        n++;
      }
      if ( fSrc.isDirectory() && recurse )
      {
        n = n + dirCopy(fSrc, fTgt, recurse);
      }
    }
    return n;
  }

  /** Writes the supplied string to the specified file. */
  public static void fileWrite(String text, File target)
  {
    File fParent = target.getParentFile();
    if ( fParent != null )
    {
      fParent.mkdirs();
    }
    FileOutputStream fos = null;
    PrintWriter pw = null;
    try
    {
      fos = new FileOutputStream(target);
      pw = new PrintWriter(fos);
      pw.println(text);
      pw.flush();
      fos.flush();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      if ( pw != null )
      {
        try
        {
          pw.close();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      if ( fos != null )
      {
        try
        {
          fos.close();
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Reads the specified text file as a string
   * 
   * @param fin
   *          File to be read
   * @return String with file's contents
   */
  public static String fileRead(File fin)
  {
    try
    {
      String CR = System.getProperty("line.separator", "\r\n");
      StringBuilder sb = new StringBuilder();
      FileReader fr = new FileReader(fin);
      BufferedReader br = new BufferedReader(fr);
      String str;
      while ((str = br.readLine()) != null)
      {
        sb.append(str).append(CR);
      }
      br.close();
      fr.close();
      return sb.toString();
    }
    catch (FileNotFoundException e)
    {
      return "File not found " + fin.getAbsolutePath();
    }
    catch (IOException e)
    {
      return "File i/o error reading " + fin.getAbsolutePath();
    }
  }

  /**
   * Reads the specified text file as a string
   * 
   * @param fin
   *          File to be read
   * @return String with file's contents
   * @throws IOException
   */
  public static String resourceRead(String resourcePath) throws IOException
  {
    String CR = System.getProperty("line.separator", "\r\n");
    StringBuilder sb = new StringBuilder();
    InputStream is = ClassLoader.getSystemResourceAsStream(resourcePath);
    if ( is == null )
    {
      return "Resource not found:" + resourcePath + "\r\n";
    }
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);
    String str;
    while ((str = br.readLine()) != null)
    {
      sb.append(str).append(CR);
    }
    br.close();
    isr.close();
    return sb.toString();
  }

  /** Reads the file and returns it's contents as a List of lines */
  public static List<String> fileReadLines(File fin)
  {
    ArrayList<String> lst = new ArrayList<String>();
    try
    {
      FileReader fr = new FileReader(fin);
      BufferedReader br = new BufferedReader(fr);
      String str;
      while ((str = br.readLine()) != null)
      {
        lst.add(str);
      }
      br.close();
      fr.close();
    }
    catch (FileNotFoundException e)
    {
      lst.add("*** File not found " + fin.getAbsolutePath());
    }
    catch (IOException e)
    {
      lst.add("*** File i/o error reading " + fin.getAbsolutePath());
    }
    return lst;
  }

//  /** Reads a file (within a zip) and returns it as lines */
//  public static String[] zipReadLines(File fZip, String fileSpec)
//  {
//    ZipHelper zh = new ZipHelper(fZip);
//    return zh.readLines(fileSpec);
//  }

  /**
   * Returns the attributes from the manifest within the jar file provided 
   */
  public static Attributes jarGetManifestAttributes(File jar) throws IOException
  {
    URL url = jar.toURL();
    URL u = new URL("jar", "", url + "!/");
    JarURLConnection uc = (JarURLConnection) u.openConnection();
    Attributes attr = uc.getMainAttributes();
    return attr;
  }

}
