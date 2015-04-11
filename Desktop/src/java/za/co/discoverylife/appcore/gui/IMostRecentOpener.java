package za.co.discoverylife.appcore.gui;

/** Defines the openRecent method used by Most recently used file menu to open files */
public interface IMostRecentOpener
{
	/** open the file 
	 * 
	 * @param title Kind/type of file being opened
	 * @param name File specification 
	 */
	public void openRecent(String title, String name);
}
