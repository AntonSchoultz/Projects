package za.co.discoverylife.appcore.gui.screens;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import za.co.discoverylife.appcore.field.MetaFieldInfo;

/**
 * Lists the contents of a directory
 * 
 * @author Anton Schoultz (2012)
 */
public class GuiDirList extends GuiList {
	private static final long serialVersionUID = 383450945962450271L;
	public static final String PANEL_NAME = "Repository";

	// accessed directly via GUI accessors
	@MetaFieldInfo(label = "DirectoryList", hint = "List of artifacts", icon = "application")
	private List<String> directoryList = new ArrayList<String>();
	private FilenameFilter filter;

	public GuiDirList(File dir, FilenameFilter filter, boolean showSubDir) {
		super(dir.getName(), "Directory listing for " + dir.getName());
		this.filter = filter;
		fetchDirectoryListing(dir, showSubDir);
		buildScreen(this, "directoryList", 25, 3, W_LABEL_TEXT * 4 / 3);
	}

	protected void fetchDirectoryListing(File repoDir, boolean showSubDir) {
		directoryList = new ArrayList<String>();
		File[] lst = (filter == null) ? repoDir.listFiles() : repoDir.listFiles(filter);
		for (File f : repoDir.listFiles()) {
			if (f.isDirectory() && showSubDir) {
				directoryList.add("[" + f.getName() + "]");
			}
		}
		for (File f : lst) {
			if (f.isFile()) {
				directoryList.add(f.getName());
			}
		}
	}

	/** Returns number of files found */
	public int count() {
		return directoryList.size();
	}

}
