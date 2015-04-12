package za.co.discoverylife.st2git.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility class to assist in creating ZIP files.
 * 
 * @author anton11
 * 
 */
public class ZipHelper
{
  private File fZip;
  private FileOutputStream dest = null;
  private ZipOutputStream out = null;
  private int rootLength = -1;
  private int level = 0;
  private final int MAX_LVL = 4;
  private static final int BUF_SIZE = 2048;

  /**
   * CONSTRUCTOR which accepts the zipFile specifier
   * 
   * @param fZip
   *          File pointing to ZIP file
   */
  public ZipHelper(File fZip)
  {
    this.fZip = fZip;
  }

  /** Reads a file (within a ZIP) and returns it as lines (or null if not found) */
  public String[] readLines(String fileSpec)
  {
    int BUF_SIZE = 2048;
    byte data[] = new byte[BUF_SIZE];
    StringBuffer sb = new StringBuffer();
    String key = fileSpec.replace('\\', '/');
    try
    {
      ZipInputStream zin = new ZipInputStream(new FileInputStream(fZip));
      ZipEntry ze;
      int count;
      while ((ze = zin.getNextEntry()) != null)
      {
        // log.trace(ze.toString());
        if (ze.getName().replace('\\', '/').compareTo(key) == 0)
        {
          // log.debug("Found "+fileSpec);
          while ((count = zin.read(data, 0, BUF_SIZE)) != -1)
          {
            sb.append(new String(data, 0, count));
          }
          // found, so abort - no need to search further
          zin.close();
          return sb.toString().replace('\r', ' ').split("\n");
        }
      }
      zin.close();
    }
    catch (Exception e)
    {
      // log.error("problem trying to read '"+fileSpec+"' from the "+fZip.getAbsolutePath() ,e);
    }
    // log.debug("Could not find "+fileSpec);
    return null;
  }

  public void createZipFromDir(File fDir) throws Exception
  {
    if (dest == null)
    {
      dest = new FileOutputStream(fZip);
      out = new ZipOutputStream(dest);
      fZip.getParentFile().mkdirs();
    }
    rootLength = fDir.getAbsolutePath().length();
    addFolder(fDir);
    out.flush();
    out.close();
  }

  /**
   * Create the ZIP file by adding all the provided files/folders.
   * 
   * @param listFiles
   *          ArrayList<File> of files/directories to be added
   * @throws Exception
   */
  public void createZip(File... listFiles) throws Exception
  {
    for (File fAdd : listFiles)
    {
      if (rootLength < 0)
      {
        if (dest == null)
        {
          dest = new FileOutputStream(fZip);
          out = new ZipOutputStream(dest);
          fZip.getParentFile().mkdirs();
        }
        if (fAdd.isDirectory())
        {
          rootLength = fAdd.getAbsolutePath().length() + 1;
        }
        else
        {
          rootLength = fAdd.getParentFile().getAbsolutePath().length() + 1;
        }
      }
      addFileOrFolder(fAdd);
    }
    out.flush();
    out.close();
  }

  /**
   * Recursive method to add all files and folders within the supplied folder.
   * 
   * @param fDir
   *          File
   * @throws Exception
   */
  private int addFolder(File fDir)
  {
    int n = 0;
    // String name = fDir.getAbsolutePath().substring(rootLength);
    level++;
    if (level == MAX_LVL)
    {
      // log.trace("Adding: " + name + File.separator + "...");
    }
    File[] fa = fDir.listFiles();
    for (int i = 0; i < fa.length; i++)
    {
      File fFile = fa[i];
      n += addFileOrFolder(fFile);
    }
    level--;
    return n;
  }

  private int addFileOrFolder(File fFile)
  {
    BufferedInputStream origin = null;
    byte data[] = new byte[BUF_SIZE];
    if (fFile.isDirectory())
    {
      return addFolder(fFile);
    }
    else
    {
      // add the file
      String addName = fFile.getAbsolutePath().substring(rootLength);
      if (level < MAX_LVL)
      {
        // log.trace("Adding: " + addName);
      }
      try
      {
        FileInputStream fi = new FileInputStream(fFile);
        origin = new BufferedInputStream(fi, BUF_SIZE);
        ZipEntry entry = new ZipEntry(addName);
        out.putNextEntry(entry);
        int count;
        while ((count = origin.read(data, 0, BUF_SIZE)) != -1)
        {
          out.write(data, 0, count);
        }
        origin.close();
      }
      catch (Exception ex)
      {
        // log.error("ERROR adding " + addName + "(" + ex.toString() + ")", ex);
      }
    }
    return 1;
  }
}
