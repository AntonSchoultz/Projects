/**
 * TaskQueue.java
 */
package za.co.discoverylife.appcore.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Wraps an Executor Service to provide a simple multi-threaded task queue.
 * 
 * @author Anton Schoultz (2014)
 */
public class TaskQueue
{
  /** Executor to execute Tasks in sequence */
  private ExecutorService execQueue = null;

  /** Thread pool size */
  private int poolSize = 5;

  /**
   * CONSTRUCTOR which accepts the maximum number of concurrent threads
   * 
   * @param poolSize Max number of threads to run at the same time
   */
  public TaskQueue(int poolSize)
  {
    this.poolSize = poolSize;
  }

  /**
   * Default CONSTRUCTOR (sets pool size to 5)
   */
  public TaskQueue()
  {
  }

  /**
   * Changes the pool size. May only be done BEFORE the pool becomes active.
   * <br>ie before any calls to  execute(~)
   * @param poolSize
   * @throws Exception
   */
  public void setPoolSize(int poolSize) throws Exception
  {
    if ( execQueue != null )
    {
      throw new Exception("May not change pool size once the pool is active.");
    }
    this.poolSize = poolSize;
  }

  /**
   * Add a task to be run, will start when a thread is available.
   * 
   * @param runItem Runnable work item
   */
  public void execute(Runnable runItem)
  {
    if ( execQueue == null )
    {
      execQueue = Executors.newFixedThreadPool(poolSize);
    }
    try
    {
      execQueue.execute(runItem);
    }
    catch (Exception e)
    {
      System.err.println("Could not queue a task, running in-line");
      runItem.run();
    }
  }

  /**
   * Shuts down the pool and waits for all items to finish.
   * 
   * @throws InterruptedException
   */
  public void waitUntilAllDone() throws InterruptedException
  {
    execQueue.shutdown();
    execQueue.awaitTermination(600, TimeUnit.SECONDS);
    execQueue = null;
  }

  /**
   * @return the poolSize
   */
  public int getPoolSize()
  {
    return poolSize;
  }

}
