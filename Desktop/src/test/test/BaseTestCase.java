package test;

import junit.framework.TestCase;

public class BaseTestCase extends TestCase
{
  String name;

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    name = this.getClass().getSimpleName();
  }
  
  protected void wr(String msg){
    StackTraceElement[] trace = (new Exception("*")).getStackTrace();
    StackTraceElement stk = trace[1];
    System.out.println("["+name+"."+stk.getMethodName()+":"+stk.getLineNumber()+"] "+ msg);
  }
}
