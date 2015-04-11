package za.co.discoverylife.appcore.logging;

import java.util.ArrayList;
import java.util.List;

/** 
 * Acts as a relay station for logging messages
 * It is a LogListeners that reroutes messages to all registered logListeners.
 * 
 * @author anton11
 *
 */
public class LoggerHub implements ILogListener {

	protected List<ILogListener> logList = new ArrayList<ILogListener>();

	/**
	 * Implements ILogListener.clear by sending clear signal to all registered log listeners
	 */
	public void clear() {
		synchronized (logList) {
			for (ILogListener log : logList) {
				log.clear();
			}
		}
	}

	/**
	 * Implements ILogListener.close by sending close signal to all registered log listeners
	 */
	public void close() {
		synchronized (logList) {
			for (ILogListener log : logList) {
				log.close();
			}
		}
	}

	/**
	 * Implements ILogListener.doLog by sending log message to all registered log listeners
	 */
	public void doLog(int lvl, String msg, Exception e) {
		// send log message to all registered listeners
		try {
			synchronized (logList) {
				for (ILogListener log : logList) {
					try {
						log.doLog(lvl, msg, e);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Registers a log listener to receive log messages
	 * 
	 * @param logger
	 */
	public void addLogListener(ILogListener logger) {
		synchronized (logList) {
			if (!logList.contains(logger)) {
				logList.add(logger);
			}
		}
	}

	/**
	 * Unregisters a log listener (will no longer receive log messages)
	 * 
	 * @param logger
	 */
	public void removeLogListener(ILogListener logger) {
		synchronized (logList) {
			logList.remove(logger);
		}
	}

}
