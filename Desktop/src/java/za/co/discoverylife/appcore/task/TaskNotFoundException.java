package za.co.discoverylife.appcore.task;

/** 
 * Exception for when tasks can not be found by task manager
 * 
 * @author Anton Schoultz
 *
 */
public class TaskNotFoundException
		extends Exception
{
	private static final long serialVersionUID = -3668337976749350057L;

	public TaskNotFoundException() {
	}

	public TaskNotFoundException(String message) {
		super(message);
	}

	public TaskNotFoundException(Throwable cause) {
		super(cause);
	}

	public TaskNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
