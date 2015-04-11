package test.za.co.discoverylife.desktop.util;

import java.io.File;
import java.util.Set;
import test.BaseTestCase;
import za.co.discoverylife.desktop.util.ClassHelper;

public class TestClassHelper extends BaseTestCase
{
  
  public void testFindClasses()
  {
    try
    {
      Set<Class<?>> list = ClassHelper.findClasses("za.co.discoverylife.desktop");
      boolean found = false;
      for (Class<?> k : list)
      {
        wr(k.getName());
        if (ClassHelper.class.getName().compareTo(k.getName()) == 0)
        {
          found = true;
        }
      }
      assertTrue("Find ClassFinder", found);
    }
    catch (Exception e)
    {
      fail("Failed ClassFinder test " + e.getMessage());
    }
  }
  
  public void testFindApplicationHome(){
    File fDir = ClassHelper.findApplicationHome();
    wr("AppRoot="+fDir.getAbsolutePath());
  }

  public void testFindJDK(){
    try
    {
      File fDir = ClassHelper.findJDK();
      wr("JDK="+fDir.getAbsolutePath());
    }
    catch (Exception e)
    {
      fail("Failed findJDK " + e.getMessage());
    }
  }

  protected void setUp() throws Exception
  {
    super.setUp();
  }
}
