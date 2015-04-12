package za.co.discoverylife.st2git.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class PrintFile
{
  File fOut;
  FileOutputStream fos = null;
  PrintWriter pw = null;

  public PrintFile(String fileSpec)
  {
    fOut = new File(fileSpec);
  }

  public PrintFile(File file)
  {
    fOut = file;
  }
  
  public void println() throws Exception{
    open();
    pw.println();
  }

  public void println(String s) throws Exception{
    open();
    pw.println(s);
  }

  public void open() throws Exception
  {
    if (fos == null)
    {
      fos = new FileOutputStream(fOut);
      pw = new PrintWriter(fos);
    }
  }

  public void close() throws Exception
  {
    if (pw == null)
    {
      return;
    }
    StringBuilder sb = new StringBuilder();
    try
    {
      pw.flush();
      fos.flush();
    }
    catch (Exception e)
    {
      sb.append(e.getMessage()).append("\r\n");
    }
    finally
    {
      if (pw != null)
      {
        try
        {
          pw.close();
        }
        catch (Exception e)
        {
          sb.append(e.getMessage()).append("\r\n");
        }
      }
      if (fos != null)
      {
        try
        {
          fos.close();
        }
        catch (IOException e)
        {
          sb.append(e.getMessage()).append("\r\n");
        }
      }
    }
    fos = null;
    pw = null;
    if(sb.length()>0){
      throw new Exception(sb.toString());
    }
  }
}
