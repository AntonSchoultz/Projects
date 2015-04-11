package test;

import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import za.co.discoverylife.desktop.util.ClassHelper;

public class RunAllTests extends TestSuite
{
  public static Test suite()
  {
    final TestSuite s = new TestSuite();
    try
    {
      Set<Class<?>> list = ClassHelper.findClasses("test.za.co.discoverylife.desktop.util");
      for (Class<?> k : list)
      {
        //System.out.println("Found test "+k.getName());
        s.addTestSuite((Class<? extends TestCase>) k);
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return s;
  }
}
