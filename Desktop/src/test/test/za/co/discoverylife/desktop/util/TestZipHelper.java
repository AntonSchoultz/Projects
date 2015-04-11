package test.za.co.discoverylife.desktop.util;

import java.io.File;
import java.util.ArrayList;
import test.BaseTestCase;
import za.co.discoverylife.desktop.util.ClassHelper;
import za.co.discoverylife.desktop.util.ZipHelper;


public class TestZipHelper extends BaseTestCase {
  File fApp = ClassHelper.findApplicationHome();
  File fZip = new File(fApp,"test.zip");

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
  }
	
	public void testZipHelper() {
    try
    {
      //-- Create the ZIP file
      ZipHelper zf = new ZipHelper(fZip);
      ArrayList<File> list = new ArrayList<File>();
      list.add(new File(fApp,"src/java/log4j.properties"));
      zf.createZip(list);
      assertTrue("Zip file not created",fZip.exists());
      assertTrue("Zip file empty",fZip.length()>0);
      //-- Read a file from it
      ZipHelper zfr = new ZipHelper(fZip);
      String[] text = zfr.readLines("log4j.properties");
      assertTrue("Could not read zip",text.length>0);
      //-- Check not found file
      zfr = new ZipHelper(fZip);
      String[] txt = zfr.readLines("DoesNotExistFile.txt");
      assertTrue("Failed not found test",txt==null);
    }
    catch (Exception e)
    {
      fail("Failed zip "+e.getMessage());
    }
	}

}
