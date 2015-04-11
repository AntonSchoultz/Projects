package za.co.discoverylife.appcore.task;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import za.co.discoverylife.appcore.DataHolder;
import za.co.discoverylife.appcore.gui.screens.GuiLogger;
import za.co.discoverylife.appcore.logging.ILogger;
import za.co.discoverylife.appcore.logging.LogManager;

/**
 * Handles registration of modules with their entry points.
 * Can execute registered tasks in-line, spawned, or queued.
 * EntryPoints may be enabled/disabled and PropertyChange support is provided for this.
 * 
 * @author Anton Schoultz
 */
public class TaskManager
    implements ITaskConstants
{
  private static TaskManager taskManager;

  /** Map of registered tasks */
  private Map<String, TaskEntry> actionMap = new TreeMap<String, TaskEntry>();

  /** Lists of tasks per class */
  private Map<Class<? extends Object>, List<TaskEntry>> mapClassVsTaskEntry = new HashMap<Class<? extends Object>, List<TaskEntry>>();

  /** Executor to execute Tasks in sequence */
  private ExecutorService execQueue;

  /** Handles changes to enable/disable */
  private PropertyChangeSupport pcs;

  /** Default Logger for task manager */
  private ILogger tasklog = LogManager.getLogger(getClass());

  /** Tracks task number */
  int taskNo = 0;

  /**
   * CONSTRUCTS the TaskManager singleton
   */
  private TaskManager()
  {
    taskManager = this;
    pcs = new PropertyChangeSupport(taskManager);
    // start executor with lower priority
    // execQueue = Executors.newSingleThreadExecutor(new PriorityThreadFactory().lower());
    execQueue = Executors.newFixedThreadPool(1);
  }

  /** Return the singleton of this class */
  public static TaskManager getInstance()
  {
    if ( taskManager == null )
    {
      createTaskManagerInstance();
    }
    return taskManager;
  }

  /** Creates the TaskManager instance */
  private synchronized static void createTaskManagerInstance()
  {
    taskManager = new TaskManager();
  }

  /** Add a change listener to be notified whenever a task is enabled/disabled */
  public void addPropertyChangeListener(String key, PropertyChangeListener pcl)
  {
    pcs.addPropertyChangeListener(key, pcl);
  }

  /** Register all available tasks for the given class */
  public List<TaskEntry> registerModel(Class<? extends Object> modelClass)
  {
    List<TaskEntry> list = new ArrayList<TaskEntry>();
    for (Method m : modelClass.getMethods())
    {
      MetaTask ma = m.getAnnotation(MetaTask.class);
      if ( ma != null )
      {
        TaskEntry ae = new TaskEntry(modelClass, m, ma);
        actionMap.put(ae.getKey(), ae);
        list.add(ae);
        tasklog.debug("Registered " + ae.getKey());
      }
    }
    Collections.sort(list);
    mapClassVsTaskEntry.put(modelClass, list);
    return list;
  }

  /** Returns a list of task keys for the given class */
  public List<TaskEntry> listAvailable(Class<?> modelClass)
  {
    if ( modelClass == null )
      throw new NullPointerException("modelClass is null");
    List<TaskEntry> list = mapClassVsTaskEntry.get(modelClass);
    if ( list != null )
    {
      Collections.sort(list);
    }
    return list;
  }

  /** Return the TaskEntry for the specified key, throws exception if not found */
  public TaskEntry findEntry(String key)
      throws TaskNotFoundException
  {
    TaskEntry ae = actionMap.get(key);
    if ( ae == null )
    {
      throw new TaskNotFoundException("Action for '" + key + "' is not registered.");
    }
    return ae;
  }

  /** Enables/disables all methods for the given class */
  public void setEnabledAll(Class<?> k, boolean enabled)
  {
    List<TaskEntry> lst = listAvailable(GuiLogger.class);
    for (TaskEntry te : lst)
    {
      setEnabled(te.getKey(), enabled);
    }

  }

  /** Enable/disable the task specified by the provided key */
  public void setEnabled(String key, boolean enabled)
  {
    try
    {
      TaskEntry ae = findEntry(key);
      ae.setEnabled(enabled);
      // let interested partied know that something about this task has changed
      String ky = key;// ae.getModelKey();
      pcs.firePropertyChange(new PropertyChangeEvent(taskManager, ky, null, ae));
    }
    catch (Exception e)
    {
    }
  }

  /**
   * Returns true if the task, specified by the key, exists and is enabled
   * otherwise returns false
   */
  public boolean isEnabled(String key)
  {
    try
    {
      TaskEntry ae = findEntry(key);
      return ae.isEnabled();
    }
    catch (Exception e)
    {
    }
    return false;
  }

  /** Shutdown and wait for tasks to finish */
  public void waitDone(int secs)
  {
    try
    {
      Thread.sleep(250);
      System.out.println("TaskMan.waitDone():Shutting down task queue ... (timeout=" + secs + "s)");
      execQueue.shutdown();
      execQueue.awaitTermination(secs, TimeUnit.SECONDS);
      execQueue.shutdownNow();
    }
    catch (InterruptedException e)
    {
      System.out.println("TaskMan.waitDone():Problem shutting down task queue " + e.getMessage());
      execQueue.shutdownNow();
      System.out.println("TaskMan.waitDone():Done");
    }
  }

  /**
   * Create a Task for the specified key and subject
   * 
   * @param actionKey
   *          String specifies the key "Class@method"
   * @param subject
   *          Object to be acted on
   * @param log
   *          ILogger to use for logging task actions
   * @return Task (Runnable)
   * @throws Exception
   */
  public Task getActionTask(String actionKey, Object subject, ILogger log)
      throws TaskNotFoundException
  {
    if ( subject == null )
    {
      subject = getDefaultSubject(actionKey);
      if ( subject == null )
        throw new NullPointerException("Unable to get default subject object for " + actionKey);
    }
    if ( log == null )
    {
      log = tasklog;
    }
    TaskEntry entry = findEntry(actionKey);
    return new Task(entry, subject, log);
  }

  /** Fire a list of runnable to execute, in sequence, in a separate thread */
  public void doTaskList(int how, Runnable... ra)
  {
    RunList runList = new RunList(ra);
    doTask(runList, how);
  }

  /**
   * Create task for the provided key and then execute it in-line. <br>
   * The subject object is fetched from DataHolder based on the model class name
   */
  public void doTask(String key, int how)
  {
    Object obj = getDefaultSubject(key);
    if ( obj != null )
    {
      doTask(obj, key, how);
    }
    else
    {
      tasklog.error("Unable to get data object to " + TASK[how] + " task for " + key);
    }
  }

  protected Object getDefaultSubject(String key)
  {
    String[] sa = key.split("@");
    Object obj = DataHolder.recall(sa[0] + "@");
    return obj;
  }

  /**
   * Create task for the provided key to act on the provided object
   * and then execute it in-line.
   */
  public void doTask(Object object, String key, int how)
  {
    try
    {
      Task t = getActionTask(key, object, tasklog);
      doTask(t, how);
    }
    catch (TaskNotFoundException e)
    {
      tasklog.error("Unable to locate task " + key + " to run as " + TASK[how]);
    }
  }

  /**
   * Execute the provided task in a thread based on 'how'
   * 
   * @param task
   *          Runnable to be executed
   * @param how
   *          How it should run
   *          <ul>
   *          <li><b>TASK_IN_LINE</b> run in the current thread, return when done.
   *          <li><b>TASK_SPAWN</b> run in it's own thread, return immediately
   *          <li><b>TASK_QUEUE</b> queue for execution and return immediately
   *          </ul>
   */
  public void doTask(Runnable task, int how)
  {
    switch (how)
    {
      case TASK_IN_LINE :
        task.run();
        break;
      case TASK_SPAWN :
        Thread t = new Thread(task);
        t.setName("TaskMan-" + (taskNo++));
        t.setDaemon(true);
        t.start();
        break;
      case TASK_AWT_EVENT :
        javax.swing.SwingUtilities.invokeLater(task);
        break;
      case TASK_QUEUE_DELAYED :
        if ( task instanceof Task )
        {
          ((Task) task).setmSecDelay(2000);
        }
      case TASK_QUEUE :
        execQueue.execute(task);
        break;
      default :
        throw new RuntimeException("Method of execution is invalid for " + task.toString());
    }
  }

}
