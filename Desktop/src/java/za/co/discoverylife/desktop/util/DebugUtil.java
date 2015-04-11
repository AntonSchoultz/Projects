package za.co.discoverylife.desktop.util;

public class DebugUtil
{

  public DebugUtil()
  {
  }

  public static String getStackMessage(String msg)
  {
    return getStackMessage(new Exception(msg));
  }

  public static String notImplemented(Object obj)
  {
    Exception e = new Exception("Not yet implemented in " + obj.getClass().getName());
    StackTraceElement[] trace = e.getStackTrace();
    StackTraceElement stk = trace[0];
    int m = Math.max(10, trace.length);
    int i;
    for (i = 0; i < m; i++)
    {
      stk = trace[i];
      if ( !stk.getMethodName().equalsIgnoreCase("notImplemented") )
      {
        break;
      }
    }
    StringBuilder sb = new StringBuilder();
    sb.append("Method " + stk.getMethodName() + "(~) " + e.getMessage() + ":" + stk.getLineNumber());
    sb.append("\r\n\t");
    sb.append(stk.getClassName() + "." + stk.getMethodName() + ":" + stk.getLineNumber());
    //    stk = trace[i + 1];
    //    sb.append("\r\n\t");
    //    sb.append(stk.getClassName() + "." + stk.getMethodName() + ":" + stk.getLineNumber());
    return sb.toString();
  }

  public static String getStackMessage(Exception e)
  {
    StackTraceElement[] trace = e.getStackTrace();
    StackTraceElement stk = trace[1];
    StringBuilder sb = new StringBuilder();
    sb.append(stk.getClassName() + "." + stk.getMethodName() + ":" + stk.getLineNumber() + " " + e.getMessage());
    sb.append("\r\n");
    int m = Math.max(10, trace.length);
    for (int i = 2; i < m; i++)
    {
      stk = trace[i];
      sb.append(stk.getClassName() + "." + stk.getMethodName() + ":" + stk.getLineNumber());
      sb.append("\r\n");
    }
    return sb.toString();
  }
}
