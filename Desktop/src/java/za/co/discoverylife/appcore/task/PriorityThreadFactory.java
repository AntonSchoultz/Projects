package za.co.discoverylife.appcore.task;

import java.util.concurrent.ThreadFactory;

/**
 * Thread factory which can provide threads of higher/lower priority
 * 
 * @author Anton Schoultz
 */
public class PriorityThreadFactory
		implements ThreadFactory
{
	private int priority;

	/** CONSTRUCTOR */
	public PriorityThreadFactory() {
		priority = Thread.currentThread().getPriority();
	}

	/** Reduce priority */
	public PriorityThreadFactory lower() {
		if (priority > Thread.MIN_PRIORITY) {
			priority--;
		}
		return this;
	}

	/** Increrase priority */
	public PriorityThreadFactory higher() {
		if (priority < Thread.MAX_PRIORITY) {
			priority++;
		}
		return this;
	}

	/** Set thread priority */
	public PriorityThreadFactory(int threadPriority) {
		this.priority = threadPriority;
	}

	/** Create new Thread for the provided runnable, setting the priority */
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setPriority(priority);
		return t;
	}

}
