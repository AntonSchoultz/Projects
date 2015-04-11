package za.co.discoverylife.appcore.task;

/** 
 * States for tasks 
 * 
 * @author Anton Schoultz
 */
public interface IStateConstants {
	public static final int STATE_IDLE = 0;
	public static final int STATE_STANDBY = 1;
	public static final int STATE_ACTIVE = 2;

	public static final int STATE_DONE = 3;
	public static final int STATE_CANCEL = 4;
	public static final int STATE_FAIL = 5;

	public static final String[] STATE = {"Idle", "Stand By", "Active", "Done",
			"Canceled", "Failed"};

}
