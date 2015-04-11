package za.co.discoverylife.appcore.host;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import za.co.discoverylife.appcore.logging.ILogger;
import za.co.discoverylife.appcore.logging.LogManager;

/**
 * Server listens for Sample information on TCP/IP pipe and logs
 * summary of the samples.
 *
 * Creates and manages a pool of Listener worker threads.
 * Each worker thread can receive information message via a TCP/IP pipe.
 * These are all collected and summarized via this server class.
 * The collection process clears all message buffers ready for the
 * next sampling period.
 *
 * @author Anton Schoultz - Feb 2009
 * @version 1.0
 */
public abstract class AbstractServer
    extends AbstractThread
{
  /** port that we are listening to */
  protected int port = 9999;

  /** timeout on client connections */
  protected int timeout = 5000;

  /** max # worker threads */
  protected int max_workers = 5;

  /** Where worker threads stand idle */
  protected Vector<AbstractWorkerThread> idlePool = new Vector<AbstractWorkerThread>();

  /** Where workers are while they are active */
  protected Vector<AbstractWorkerThread> activePool = new Vector<AbstractWorkerThread>();

  /** the main server socket */
  protected ServerSocket ss;

  protected String hostName = getClass().getSimpleName();

  protected ILogger log = LogManager.getLogger(getClass());

  /** Used to keep track of hit ratio */
  protected long requests = 0; // count of incoming socket requests
  protected long hit = 0; // no of those requests for which a thread was available

  /**
   * CONSTRUCTOR
   */
  public AbstractServer(int port, int timeOut, int maxWorkers)
      throws IOException
  {
    this.port = port;
    this.timeout = timeOut;
    this.max_workers = maxWorkers;
    log.info("ServiceEventServer created port:" + port
        + ", timeout=" + timeout + ", workers=" + max_workers);
  }

  /** When the thread starts, the server starts */
  public void run()
  {
    Thread.currentThread().setName(hostName + "-Host");
    try
    {
      ss = new ServerSocket(port);
      log.report("Started");
    }
    catch (IOException ex4)
    {
      log.error("Problem creating listener", ex4);
      return;
    }
    try
    {
      ss.setSoTimeout(1000);
      /* create & startup worker threads and park them in the idlePool */
      prepareIdlePool();
      while (!isShuttingDown)
      {
        try
        {
          Socket s = ss.accept();
          s.setSoTimeout(this.timeout);
          s.setSoLinger(false, 0);
          requests++;
          if ( !isShuttingDown )
          {
            AbstractWorkerThread w = getWorker(); // get available worker - create if required
            w.setSocket(s); // hand connection over for processing - notifies worker to loop
          } // end if running
        }
        catch (IOException ex3)
        {
          // socket timeout so simply re-try the accept
        }
      } //while not shutdown
      // stop accepting requests
      try
      {
        if ( ss != null )
        {
          ss.close(); // stop listening on port
        }
      }
      catch (IOException ex)
      {
        log.error("Problem closing listener socket:" + ex.getMessage(), ex);
        ex.printStackTrace();
      }
      // signal kill to all idle workers
      while (activePool.size() > 0)
      {
        AbstractWorkerThread worker = null;
        synchronized (activePool)
        {
          if ( activePool.size() > 0 )
          {
            worker = activePool.elementAt(0);
          }
        }
        if ( worker != null )
        {
          worker.shutDown();
          try
          {
            worker.join();
          }
          catch (InterruptedException ex2)
          {
            log.error("Problem with worker thread (join)" + ex2.getMessage(), ex2);
          }
        }
      }
      while (idlePool.size() > 0)
      { //while there are active workers
        AbstractWorkerThread worker = null;
        synchronized (idlePool)
        {
          if ( idlePool.size() > 0 )
          {
            worker = idlePool.elementAt(0);
            idlePool.remove(0);
          }
        }
        if ( worker != null )
        {
          worker.shutDown();
          try
          {
            worker.join();
          }
          catch (InterruptedException ex2)
          {
            log.error("Problem with worker thread (join)" + ex2.getMessage(), ex2);
          }
        }
      } //while there are active workers
    }
    catch (IOException ex1)
    {
      log.error("Problem in listener socket:" + ex1.getMessage(), ex1);
      //ServiceMonitor.abort(ABORT_MSG);
    }
    isDone = true;
    log.info("Server managed to achieve a hit rate of " + getHitRate() + " %");
  }

  /** Accept an exhausted worker and either kill it off or recycle it into idlePool */
  public synchronized boolean retireWorker(AbstractWorkerThread worker)
  {
    flush(worker);
    //take worker out of active pool and place into retired pool
    activePool.remove(worker);
    // put worker back in line (if not too many)
    if ( idlePool.size() < this.max_workers )
    {
      idlePool.add(worker);
      return false;
    }
    else
    {
      return true; // signal worker no longer required so it will break run() loop
    }
  }

  /* Creates some worker threads and parks them in idle state */
  private void prepareIdlePool()
  {
    for (int i = 0; i < max_workers; ++i)
    {
      AbstractWorkerThread w = createAndStartWorkerThread(); // create & start a worker
      idlePool.addElement(w); //place in pool
    }
  }

  /* Take worker from idle pool (or create new one)
   * activate worker and return it for duty */
  private AbstractWorkerThread getWorker()
  {
    AbstractWorkerThread w;
    synchronized (idlePool)
    {
      if ( !idlePool.isEmpty() )
      {
        // idle worker is available so use it
        hit++;
        w = idlePool.elementAt(0);
        idlePool.removeElementAt(0);
      }
      else
      {
        // none on standby so create a new one
        w = createAndStartWorkerThread();
      }
    }
    // place into the active Pool & return the worker
    synchronized (activePool)
    {
      activePool.add(w);
    }
    return w;
  }

  /** Returns the hit rate of the pool */
  public long getHitRate()
  {
    if ( requests == 0 )
    {
      return 100; // no requests - fully serviced :-)
    }
    return (hit * 100) / requests;
  }

  /** creates a worker thread to handle requests with */
  private AbstractWorkerThread createAndStartWorkerThread()
  {
    AbstractWorkerThread w = createWorkerThread();
    w.start();
    return w;
  }

  /** creates a worker thread to handle requests with */
  public abstract AbstractWorkerThread createWorkerThread();

  /** Flush the worker thread (called before retiring it) */
  public abstract void flush(AbstractWorkerThread worker);

}
