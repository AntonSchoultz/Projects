package za.co.discoverylife.appcore.task;

import java.lang.reflect.Method;

import za.co.discoverylife.appcore.logging.ILogger;
import za.co.discoverylife.appcore.logging.LogManager;

/**
 * A runnable task that can evoke a method on a model object.
 * This is usually created via the {@link TaskManager}.getActionTask(~) method.
 * 
 * @author Anton Schoultz
 */
public class Task
		extends BaseTask
{
	private TaskEntry entry;
	private Object subject;
	private ILogger log;
	private long mSecDelay=0;
	private Object dataObject=null;

	/**
	 * CONSTRUCTS an action task
	 * 
	 * @param entry
	 *          ActionEntry which defines the action to be performed
	 * @param subject
	 *          The model Object that the action will be performed on
	 * @param log
	 *          ILogger to log action details to (usually the requestor's log)
	 * @throws Exception
	 */
	public Task(TaskEntry entry, Object subject, ILogger log) throws NullPointerException {
		super();
		this.entry = entry;
		this.subject = subject;
		this.log = log;
		if (this.log == null) {
			this.log = LogManager.getLogger(subject.getClass());
		}
		if (subject == null) {
			throw new NullPointerException("Can not create a task for a null subject.");
		}
	}

	/**
	 * Implements Runnable to invoke the method on the model object.
	 */
	public void executeTask() {
		Thread.currentThread().setName(entry.getKey());
		if (!entry.isEnabled()) {
			log.info("Action is disabled, not executing " + entry.getKey());
			return;
		}
		try {
			if(mSecDelay>0){
				Thread.sleep(mSecDelay);
			}
			// log.clear();
			log.debug("Starting action " + entry.getKey());
			Method m = entry.getMethod();
			Class<?>[] pta = m.getParameterTypes();
			Object[] oa = new Object[pta.length];
			if(pta.length>0){
				oa[0]=dataObject;
			}
			m.invoke(subject, oa);
			log.debug("Completed action " + entry.getKey());
		} catch (Exception e1) {
			if (log != null) {
				log.error("Problem executing action " + entry.toString(), e1);
			} else {
				System.err.println("Problem executing action " + entry.toString() + "\r\n" + e1.getMessage());
				e1.printStackTrace();
			}
		}
	}

	public long getmSecDelay() {
		return mSecDelay;
	}

	public void setmSecDelay(long mSecDelay) {
		this.mSecDelay = mSecDelay;
	}

	public Object getDataObject() {
		return dataObject;
	}

	public void setDataObject(Object dataObject) {
		this.dataObject = dataObject;
	}

}
