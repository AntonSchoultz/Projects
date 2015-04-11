package za.co.discoverylife.appcore.task;

/**
 * Allows several thread jobs to be chained together into a list,
 * which are then executed sequentially by this thread.
 * 
 * @author Anton Schoultz
 */
public class RunList
		extends BaseTask
{
	Runnable[] runList;

	/** CONSTRUCTOR which accepts a list of runnables to be executed in order */
	public RunList(Runnable... runnables) {
		runList = runnables;
	}

	/** Executes the runnables in the provided order */
	public void executeTask() {
		Thread.currentThread().setPriority(3);
		for (Runnable r : runList) {
			r.run();
		}
	}

}
