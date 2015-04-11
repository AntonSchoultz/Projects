package za.co.discoverylife.appcore;

/**
 * Provides a simple base class which can echo messages to the console
 * with information about where the message method was called.
 * 
 * @author ANTON11
 *
 */
public class DebugObject
{

  public static void wr(String s)
  {
    //System.out.println("WR:" + s + "\r\n" + exceptionToString(new Exception()));
    StackTraceElement[] trace = (new Exception().getStackTrace());
    StackTraceElement stk = trace[1];
    System.err.println(stk.getClassName() + "." + stk.getMethodName() + ":" + stk.getLineNumber() + " " + s);
    int m = Math.min(10, trace.length);
    for (int i = 2; i < m; i++)
    {
      stk = trace[i];
      System.out.println(stk.getClassName() + "." + stk.getMethodName() + ":" + stk.getLineNumber());
    }
  }

  public static String trace(int back, String s)
  {
    StackTraceElement[] trace = (new Exception().getStackTrace());
    StackTraceElement stk = trace[back];
    if ( stk == null )
    {
      stk = trace[0];
      s = "*STACKERROR** " + s;
    }
    StringBuilder sb = new StringBuilder();
    sb.append(stk.getClassName() + "." + stk.getMethodName() + "():" + stk.getLineNumber() + " " + s);
    for (int i = 1; i < 3; i++)
    {
      sb.append("\r\n--");
      stk = trace[back + i];
      sb.append(stk.getClassName() + "." + stk.getMethodName() + "():" + stk.getLineNumber());
    }
    sb.append("\r\n--");
    return sb.toString();
  }

  /** Returns a string description of the provided exception */
  public static String exceptionToString(Exception e)
  {
    try
    {
      StringBuilder sb = new StringBuilder();
      StackTraceElement[] trace = e.getStackTrace();
      StackTraceElement stk = trace[1];
      if ( stk != null )
      {
        sb.append(stk.getClassName() + "." + stk.getMethodName() + ":" + stk.getLineNumber() + " " + e.getMessage());
        sb.append("\r\n");
        int m = Math.min(10, trace.length);
        for (int i = 2; i < m; i++)
        {
          stk = trace[i];
          sb.append(stk.getClassName() + "." + stk.getMethodName() + ":" + stk.getLineNumber());
          sb.append("\r\n");
        }
      }
      else
      {
        sb.append(e.getMessage());
      }
      return sb.toString();
    }
    catch (Exception e1)
    {
      System.err.println("Could not extract exception error due to " + e1.getMessage());
      e.printStackTrace();
      return e.toString();
    }
  }

}
