package za.co.discoverylife.appcore.logging;

/**
 * Logging API interface
 * 
 * @author anton11
 */
public interface ILogListener
		extends ILoggerConstants {
	/** Close old logger, ready to start a new one (close/clear) */
	public void clear();

	/** Close old logger at end */
	public void close();

	/** Logs the actual message (debug,info,warn etc call this one) */
	public void doLog(int lvl, String msg, Exception e);

}
