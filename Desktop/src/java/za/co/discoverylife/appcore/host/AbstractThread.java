package za.co.discoverylife.appcore.host;

/**
 * Base class for threads which supports forced shutdown control.
 *
 * @author Anton Schoultz 2014
 */
public class AbstractThread
    extends Thread
{
  protected volatile boolean isShuttingDown = false;
  protected volatile boolean isDone = false;

  /** 
   * Shuts down the worker thread and waits (up to 10sec) for it to die
   */
  public void shutDown()
  {
    shutDownWait(10);
  }

  /** Shuts down the worker thread and waits for it to die
   * @param maxWaitSeconds max no of seconds to wait for shutdown to finish.
   */
  public void shutDownWait(int maxWaitSeconds)
  {
    this.isShuttingDown = true; // causes run() loop to exit (eventually)
    synchronized (this)
    {
      this.notify();
    }
    int stopgap = maxWaitSeconds;
    while (!isDone && --stopgap > 0)
    {
      try
      {
        Thread.sleep(1000);
      }
      catch (InterruptedException ex)
      {
      }
    }
  }

  /** returns true if the thread has finished running */
  synchronized boolean isDone()
  {
    return isDone;
  }

  public void run()
  {
    while (!isShuttingDown)
    {
      // do whatever
    }
    isDone = true;
  }

}
