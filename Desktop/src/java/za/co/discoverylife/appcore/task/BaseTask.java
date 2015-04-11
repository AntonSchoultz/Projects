package za.co.discoverylife.appcore.task;

import za.co.discoverylife.appcore.logging.ILoggerConstants;

/**
 * Extends task state by adding a runnable with a required abstract method execute()
 * 
 * @author Anton Schoultz
 */
public abstract class BaseTask extends TaskState implements Runnable
{

  protected int logLevel = ILoggerConstants.LOG_INFO;

  /**
   * Implementation of run method for threads which handles state updates
   * and calls the executeTask() method - to be implemented by child class.
   */
  public void run()
  {
    setState(STATE_IDLE);
    executeTask();
  }

  /**
   * Child class should implement this method to perform the required 
   * action or use case.
   */
  public abstract void executeTask();

  public int getLogLevel()
  {
    return logLevel;
  }

  public void setLogLevel(int logLevel)
  {
    this.logLevel = logLevel;
  }

}
