package za.co.discoverylife.appcore.logging;

import za.co.discoverylife.appcore.task.CanceledException;

/**
 * Provides a hub for logging system.
 * Implements ILogger to set log level and accept logging messages,
 * which are then routed to the associated log service (ILogListener)
 * 
 * @author anton11
 */
public class Logger
		implements ILogger {
	/** Tracks the current logging detail level */
	protected int logLevel = LOG_ALL;

	private String tag;
	private ILogListener logservice;
	private String className;

	public Logger(Class<?> logSrcClass, ILogListener logservice) {
		super();
		className = logSrcClass.getSimpleName();
		this.tag = "[" + className + "] ";
		this.logservice = logservice;
	}

	public void setLabel(String lbl) {
		this.tag = "[" + className + "-" + lbl + "] ";
	}

	/**
	 * Implements ILogListener.doLog by delegating to logservice.doLog
	 */
	public void doLog(int lvl, String msg, Exception e) {
		String txt = tag + msg;
		logservice.doLog(lvl, txt, e);
		if (e != null && !(e instanceof CanceledException)) {
			showStackTrace(e);
		}
	}

	protected void showStackTrace(Throwable t) {
		StackTraceElement[] ste = t.getStackTrace();
		int n = 0;
		for (StackTraceElement se : ste) {
			report("--- [" + se.getLineNumber() + "] " + se.getClassName() + "." + se.getMethodName() + "(~)");
			n++;
			if (n >= 10)
				break;
		}
	}

	/**
	 * Implements ILogger.clear by delegating to logservice.clear
	 */
	public void clear() {
		logservice.clear();
	}

	/** returns the log detail level */
	public int getLogLevel() {
		return logLevel;
	}

	/** sets the log detail level */
	public void setLogLevel(int logLevel) {
		this.logLevel = logLevel;
	}

	protected static String getLogLevelString(int lvl) {
		if (lvl >= 0 && lvl <= LOG_TEXT.length) {
			return LOG_TEXT[lvl];
		} else {
			return "LOGLEVEL_" + lvl;
		}
	}

	protected static String getLogLevelPrefix(int lvl) {
		if (lvl >= 0 && lvl <= LOG_PREFIX.length) {
			return LOG_PREFIX[lvl];
		} else {
			return "LOGLEVEL_" + lvl;
		}
	}

	/** Returns true if the provided log level should be logged */
	protected boolean isActive(int level) {
		return logLevel <= level;
	}

	public void debug(String msg) {
		if (isActive(LOG_DEBUG)) {
			doLog(LOG_DEBUG, msg, null);
		}
	}

	public void info(String msg) {
		if (isActive(LOG_INFO)) {
			doLog(LOG_INFO, msg, null);
		}
	}

	public void report(String msg) {
		if (isActive(LOG_REPORT)) {
			doLog(LOG_REPORT, msg, null);
		}
	}

	public void warn(String msg) {
		if (isActive(LOG_WARN)) {
			doLog(LOG_WARN, msg, null);
		}
	}

	public void error(String msg, Exception e) {
		if (isActive(LOG_ERROR)) {
			doLog(LOG_ERROR, msg, e);
		}
	}

	public void error(String msg) {
		if (isActive(LOG_ERROR)) {
			doLog(LOG_ERROR, msg, null);
		}
	}

	public void close() {
	}

}
