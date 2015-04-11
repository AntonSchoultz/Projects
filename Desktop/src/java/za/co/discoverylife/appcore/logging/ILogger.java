package za.co.discoverylife.appcore.logging;

/**
 * Logging API interface
 * 
 * @author anton11
 */
public interface ILogger
		extends ILogListener
{

	/** Logs a debug message */
	public void debug(String msg);

	/** Logs an information message */
	public void info(String msg);

	/** Logs a report summary message */
	public void report(String msg);

	/** Logs a warning message */
	public void warn(String msg);

	/** Logs an error message (with exception) */
	public void error(String msg, Exception e);

	/** Logs an error message */
	public void error(String msg);

	/** returns the current logging level */
	public int getLogLevel();

	/** resets the current logging level */
	public void setLogLevel(int logLevel);

	public void close();

	public void setLabel(String lbl);
}
