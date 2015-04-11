package za.co.discoverylife.appcore.logging;

/** Defines log levels and their descriptions */
public interface ILoggerConstants
{
	// logging events are assigned negative typeIDs (easier to split)
	public static final int LOG_TRACE = 1;
	public static final int LOG_DEBUG = 2;
	public static final int LOG_INFO = 3;
	public static final int LOG_REPORT = 4;
	public static final int LOG_WARN = 5;
	public static final int LOG_ERROR = 6;
	public static final int LOG_FATAL = 7;

	public static final int LOG_ALL = 0;
	public static final int LOG_OFF = 99;

	public static final String[] LOG_TEXT =
			{ "0", "trace", "debug", "info", "report", "warn", "error", "fatal" };

	public static final String[] LOG_PREFIX =
	{ "    :", " TRC:", "DBUG:", "INFO:", " RPT:", "WARN:", " ERR:", "FATL:" };

}
