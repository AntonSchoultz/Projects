package za.co.discoverylife.appcore.logging;

/**
 * Implements ILogListener to provide
 * simple logging to System.out (System.err for ERRORS)
 * This is a singleton class.
 * 
 * @author anton11
 */
public class CommandLineLogger
		implements ILogListener
{
	private static CommandLineLogger conlog = null;

	/** CONSTRUCTOR */
	private CommandLineLogger() {
	}

	/** returns the singleton of the logger, creating it if needed */
	public static CommandLineLogger getInstance() {
		if (conlog == null) {
			create();
		}
		return conlog;
	}

	/** Creates a logger if one does not already exist */
	private static synchronized void create() {
		if (conlog == null) {
			conlog = new CommandLineLogger();
		}
	}

	/** Implements {@link ILogListener}.doLog(~) to display log messages on System out/err */
	public synchronized void doLog(int lvl, String msg, Exception e) {
		// int p = Thread.currentThread().getPriority();
		String outmsg = Thread.currentThread().getId() + ":" + LOG_PREFIX[lvl] + msg;
		if (lvl == LOG_ERROR) {
			System.err.println(outmsg);
			if (e != null) {
				e.printStackTrace();
			}
		} else {
			System.out.println(outmsg);
		}
	}

	public void clear() {
		System.out.println("----------------------------------------------------------------------");
	}

	public void close() {
		System.out.println("======================================================================");
	}

}
