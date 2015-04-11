package za.co.discoverylife.appcore.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import za.co.discoverylife.appcore.logging.ILogger;
import za.co.discoverylife.appcore.logging.LogManager;

/**
 * Utility class to assist in creating ZIP files.
 * 
 * @author anton11
 *
 */
public class ZipFile
{
	private File fZip;
	private FileOutputStream dest = null;
	private ZipOutputStream out = null;
	private int rootLength = -1;
	private int level = 0;
	private final int MAX_LVL = 4;
	private ILogger log = LogManager.getLogger(getClass());
	private static final int BUF_SIZE = 2048;

	public ZipFile(File fZip) {
		this.fZip = fZip;
	}

	public void createZip(ArrayList<File> listFiles) throws Exception {
		for (File fAdd : listFiles) {
			if (rootLength < 0) {
				if (dest == null) {
					dest = new FileOutputStream(fZip);
					out = new ZipOutputStream(dest);
					fZip.getParentFile().mkdirs();
				}
				if (fAdd.isDirectory()) {
					rootLength = fAdd.getAbsolutePath().length()+1;
				} else {
					rootLength = fAdd.getParentFile().getAbsolutePath().length()+1;
				}
			}
			addFileOrFolder(fAdd);
		}
		out.flush();
		out.close();
	}

	/**
	 * Recursive method to add all files and folders within the supplied folder.
	 * 
	 * @param fDir
	 *          File
	 * @throws Exception
	 */
	private int addFolder(File fDir) {
		int n = 0;
		String name = fDir.getAbsolutePath().substring(rootLength);
		level++;
		if (level == MAX_LVL) {
			log.info("Adding: " + name + File.separator + "...");
		}
		File[] fa = fDir.listFiles();
		for (int i = 0; i < fa.length; i++) {
			File fFile = fa[i];
			n += addFileOrFolder(fFile);
		}
		level--;
		return n;
	}

	private int addFileOrFolder(File fFile) {
		BufferedInputStream origin = null;
		byte data[] = new byte[BUF_SIZE];
		if (fFile.isDirectory()) {
			return addFolder(fFile);
		} else {
			// add the file
			String addName = fFile.getAbsolutePath().substring(rootLength);
			if (level < MAX_LVL) {
				log.info("Adding: " + addName);
			}
			try {
				FileInputStream fi = new FileInputStream(fFile);
				origin = new BufferedInputStream(fi, BUF_SIZE);
				ZipEntry entry = new ZipEntry(addName);
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUF_SIZE)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			} catch (Exception ex) {
				log.error("ERROR adding " + addName + "(" + ex.toString() + ")", ex);
			}
		}
		return 1;
	}

}
