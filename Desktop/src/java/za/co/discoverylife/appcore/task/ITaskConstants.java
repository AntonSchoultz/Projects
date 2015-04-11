package za.co.discoverylife.appcore.task;

/** 
 * Defines the modes in which a task may be executed
 * 
 * @author Anton Schoultz
 */
public interface ITaskConstants
{

	public static final int TASK_IN_LINE = 0;
	public static final int TASK_SPAWN = 1;
	public static final int TASK_QUEUE = 2;
	public static final int TASK_AWT_EVENT = 3;
	public static final int TASK_QUEUE_DELAYED = 4;

	public static final String[] TASK =
	{ "in-line", "spawned", "queued", "awt","delayed" };

}