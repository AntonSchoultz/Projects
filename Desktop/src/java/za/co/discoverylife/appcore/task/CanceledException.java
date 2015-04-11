package za.co.discoverylife.appcore.task;

/**
 * Exception to report when a task has been Canceled / Aborted
 * 
 * @author Anton Schoultz
 */
public class CanceledException
		extends Exception
{
	private static final long serialVersionUID = -6475502319114264952L;

	public CanceledException() {
	}

	public CanceledException(String arg0) {
		super(arg0);
	}

	public CanceledException(Throwable arg0) {
		super(arg0);
	}

	public CanceledException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
