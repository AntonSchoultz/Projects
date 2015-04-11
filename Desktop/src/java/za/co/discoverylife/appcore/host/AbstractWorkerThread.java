package za.co.discoverylife.appcore.host;

import java.io.IOException;
import java.net.Socket;

/**
 * Listener worker thread which receives and queues messages sent to it
 * via TCP/IP pipe. Messages can be periodically collected, clearing the
 * queue.
 *
 * @author Anton Schoultz
 * @version 1.0
 */
public abstract class AbstractWorkerThread
    extends AbstractThread
{
  protected static int threadId = 0;
  protected Socket s = null;
  protected AbstractServer server = null;

  /** CONSTRUCTOR holds a reference to the server (for thread pooling) */
  public AbstractWorkerThread(AbstractServer server)
  {
    this.server = server;
    this.setName(getClass().getSimpleName() + "#" + threadId);
    threadId++;
    this.s = null;
  }

  /** Sets the socket to be processed and notifies the worker to get going on it */
  public void setSocket(Socket s)
  {
    this.s = s;
    synchronized (this)
    {
      notify();
    }
  }

  /** Thread's run method waits for socket to be set */
  public void run()
  {
    while (!isShuttingDown)
    {
      if ( s == null )
      {
        try
        {
          synchronized (this)
          {
            wait(); // wait for notify() by setSocket() or shutDown()
          }
        }
        catch (InterruptedException e)
        {
          continue; // Interupted by shutdown method ...
        }
      }
      if ( s != null )
      {
        handleConnection();
        try
        {
          s.close();
        }
        catch (IOException ex1)
        {
        }
        s = null;
        if ( server != null )
        {
          if ( server.retireWorker(this) )
          {
            break;
          }
        }
        else
        {
          break;
        }
      } //end if s!=null
    } //wend
    isDone = true;
  }

  /**
   * Process the incoming stream, and send out the response.
   */
  public abstract void handleConnection();

}
