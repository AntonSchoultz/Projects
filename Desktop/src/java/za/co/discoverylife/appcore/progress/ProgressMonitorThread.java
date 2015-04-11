package za.co.discoverylife.appcore.progress;

public class ProgressMonitorThread implements IProgressListener, Runnable
{
  IProgressListener targetListener;
  private Thread t;
  private volatile boolean active = true;

  private long toDoCount = 100;
  private long count = 0;
  private long delay = 100;

  /** 
   * CONSTRUCTOR which takes the target listener as an argument.
   * Delay is defaulted to 100mSec
   * 
   * @param targetListener IPorgressListener to be updated with progress
   */
  public ProgressMonitorThread(IProgressListener targetListener)
  {
    this(targetListener, 100);
  }

  /** 
   * CONSTRUCTOR which takes the target listener as an argument and a delay time
   * 
   * @param targetListener IPorgressListener to be updated with progress
   * @param mSecDelay delay between updates in mSec. (default is 100)
   */
  public ProgressMonitorThread(IProgressListener targetListener, long mSecDelay)
  {
    this.targetListener = targetListener;
    delay = mSecDelay;
    t = new Thread(this);
    t.start();
  }

  public synchronized void kill()
  {
    while (active)
    {
      active = false;
      try
      {
        Thread.sleep(10);
      }
      catch (InterruptedException e)
      {
      }
    }
  }

  public void run()
  {
    while (active)
    {
      if ( targetListener != null )
      {
        targetListener.setToDoCount(toDoCount);
        targetListener.setDoneCount(count);
      }
      try
      {
        Thread.sleep(delay);
      }
      catch (InterruptedException e)
      { /* ignore exceptions */
      }
    }
  }

  public String toString()
  {
    return count + "/" + toDoCount;
  }

  public void update(long count, long toDoCount)
  {
    this.toDoCount = toDoCount;
    this.count = count;
  }

  public void setToDoCount(long toDoCount)
  {
    this.toDoCount = toDoCount;
  }

  public void setDoneCount(long count)
  {
    this.count = count;
  }

  public void setAllDone()
  {
    kill();
    if ( targetListener != null )
    {
      targetListener.setAllDone();
    }
  }

}
