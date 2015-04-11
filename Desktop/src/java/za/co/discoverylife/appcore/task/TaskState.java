package za.co.discoverylife.appcore.task;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import za.co.discoverylife.appcore.progress.IProgressListener;
import za.co.discoverylife.desktop.util.DateTime;

/**
 * Provides Abort capability and timer for tasks.
 * 
 * @author Anton Schoultz
 */

public class TaskState implements IStateConstants, IProgressListener
{

  /** holds the state of this task 1idle/active/done/fail etc */
  private int state = STATE_IDLE;

  /** Timer - duration of the job in mSec */
  private long timer = 0;
  private long duration = 0;

  protected Exception exception;

  protected String taskName = "";

  private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  protected long toDoCount = 0;
  protected long doneCount = 0;

  /**
   * Set the state of this thread (as per BaseTask constants STATE_xxxxx) Also takes care or starting and stopping the timer.
   * 
   * @param state
   *          one of STATE_xxxx
   */
  public synchronized void setState(int state)
  {
    int oldState = this.state;
    switch (state)
    {
      case STATE_ACTIVE :
        startTimer();
        state = STATE_ACTIVE;
        break;
      case STATE_CANCEL :
        stopTimer();
        if ( this.state < STATE_DONE )
        {
          this.state = state;
        }
        break;
      case STATE_DONE :
      case STATE_FAIL :
        stopTimer();
        if ( this.state < STATE_DONE )
        {
          this.state = state;
        }
        break;
      default :
        this.state = state;
        break;
    }
    pcs.firePropertyChange("state", oldState, state);
    notifyAll();
  }

  /** Sets the name of the executing thread to class name + reference */
  public void setThreadName(String ref)
  {
    if ( ref != null )
    {
      taskName = getClass().getSimpleName() + "-" + ref;
    }
    else
    {
      taskName = getClass().getSimpleName();
    }
    Thread.currentThread().setName(taskName);
  }

  public String getState()
  {
    return STATE[state];
  }

  public void waitUntilFinished() throws InterruptedException
  {
    synchronized (this)
    {
      while (state < STATE_DONE && !isCanceled())
      {
        wait(100);
      }
    }
  }

  /**
   * Returns true if the state is >= FAIL
   * 
   * @return
   */
  public boolean failed()
  {
    return state >= STATE_FAIL;
  }

  /** Request that the process be aborted */
  public synchronized void fail(Exception exception)
  {
    this.exception = exception;
    setState(STATE_FAIL);
  }

  /** Request that the process be canceled */
  public synchronized void cancel()
  {
    setState(STATE_CANCEL);
  }

  /** Any processing required before canceling */
  public void halt()
  {
    cancel();
  }

  /**
   * If task failed it throws the failure exception, otherwise checks if canceled, if so throws canceled exception.
   * 
   * @throws Exception
   */
  public void checkOK() throws Exception, CanceledException
  {
    if ( state == STATE_FAIL )
      throw this.exception;
    checkCanceled();
  }

  public boolean isCanceled()
  {
    return state == STATE_CANCEL;
  }

  /** If the process has been requested to abort, then throw an Exception */
  public void checkCanceled() throws CanceledException
  {
    if ( state == STATE_CANCEL )
      throw new CanceledException("Canceled " + taskName);
  }

  /** Starts the timer by storing the current time (mSec) */
  protected void startTimer()
  {
    timer = System.currentTimeMillis();
    duration = 0;
  }

  /** Stops the timer storing duration in mSec */
  protected long stopTimer()
  {
    duration = System.currentTimeMillis() - timer;
    return duration;
  }

  /** Returns the timer value in milliSeconds */
  public long getTimer()
  {
    if ( duration == 0 )
    {
      stopTimer();
    }
    return duration;
  }

  public String duration()
  {
    return " in " + DateTime.elapsedAsHMS(getTimer());
  }

  /** Returns the timer value in Seconds */
  public long getTimerSecs()
  {
    return (getTimer() + 500) / 1000;
  }

  public Exception getException()
  {
    return exception;
  }

  public String getTaskName()
  {
    return taskName;
  }

  public static void snooze(int mSecs)
  {
    try
    {
      Thread.sleep(mSecs);
    }
    catch (InterruptedException e)
    {
    }
  }

  public long getDoneCount()
  {
    return doneCount;
  }

  public long getToDoCount()
  {
    return toDoCount;
  }

  /** Sets quantity to do and fires a property change  */
  public void setToDoCount(long toDo)
  {
    this.toDoCount = update("toDoCount", toDoCount, toDo);
  }

  /** Sets quantity done and fires a property change  */
  public void setDoneCount(long done)
  {
    doneCount = update("doneCount", doneCount, done);
  }

  /** Set quantity done to reflect as 100% done */
  public void setAllDone()
  {
    doneCount = update("doneCount", 0, toDoCount + 1);
    pcs.firePropertyChange("done", false, true);
  }

  /** Fires a property change event, then returns the new value */
  protected long update(String name, long oldValue, long newValue)
  {
    pcs.firePropertyChange(name, oldValue, newValue);
    return newValue;
  }

  /** Register a listener to receive progress updates */
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    pcs.removePropertyChangeListener(listener);// remove if already there
    pcs.addPropertyChangeListener(listener);
  }

  /** DeRegister a listener to receive progress updates */
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    pcs.removePropertyChangeListener(listener);
  }

}
