package za.co.discoverylife.appcore.progress;

/**
 * Defines functions used to track progress of a task.<ul>
 * <li>setToDoCount(long toDoCount)
 * <li>setProgress(long count)
 * <li>setAllDone()</ul>
 * 
 * @author Anton Schoultz (2013)
 *
 */
public interface IProgressListener
{

  /**
   * Sets number of items to be processed
   * @param toDoCount
   */
  public void setToDoCount(long toDoCount);

  /**
   * Sets number of items processed so far
   * @param count
   */
  public void setDoneCount(long count);

  /** 
   * Indicates that all items are done.
   */
  public void setAllDone();
}
