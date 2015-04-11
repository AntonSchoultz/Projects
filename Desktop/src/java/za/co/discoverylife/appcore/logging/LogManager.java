package za.co.discoverylife.appcore.logging;

/**
 * Acts as a logging hub / logger factory.
 * Log listeners may be added/removed as required.
 * 
 * @author anton11
 */
public class LogManager extends LoggerHub
		implements ILogListener {
	private static LogManager logman = null;

	/**
	 * CONSTRUCTOR is private to force singleton manager
	 */
	private LogManager() {
	}

	/**
	 * creates the singleton
	 */
	private synchronized static void createManager() {
		if (logman == null) {
			logman = new LogManager();
		}
	}

	/** Returns the singleton Log Manager */
	public static LogManager getInstance() {
		checkManagerExists();
		return logman;
	}

	/**
	 * Ensures that the singleton exists, creating it if need be.
	 */
	private static void checkManagerExists() {
		if (logman == null) {
			createManager();
		}
	}

	/**
	 * Returns a logger for the provided class
	 * 
	 * @param srcClass
	 *          class that will be doing the logging
	 * @return ILogger for initiating log messages to the manager
	 */
	public static ILogger getLogger(Class<?> srcClass) {
		checkManagerExists();
		// get logger tagged with source class name
		ILogger log = new Logger(srcClass, logman);
		return log;
	}

	/** Adds a console logger */
	public void addCommandLineLogger() {
		addLogListener(CommandLineLogger.getInstance());
	}

}
