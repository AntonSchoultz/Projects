package za.co.discoverylife.appcore.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic file filter which supports nesting and combining
 * of test conditions.
 * 
 * @author anton11
 */
public class FileFilter
		implements FilenameFilter
{
	public static final int AND = 0x00;
	public static final int OR = 0x01;
	public static final int NAND = 0x02;
	public static final int NOR = 0x03;

	private static final int NOT = 0x02;

	private int type = AND;

	private List<FilenameFilter> filters = new ArrayList<FilenameFilter>();

	/**
	 * CONSTRUCTS a new composite file filter.
	 * Combination behavior is given by type.
	 * <ul>
	 * <li><b>AND</b> all conditions must be met for file to be accepted.</li>
	 * <li><b>OR</b> any conditions may be met for file to be accepted.</li>
	 * <li><b>NAND</b> negative of AND</li>
	 * <li><b>NOR</b> negative of OR</li>
	 * </ul>
	 * 
	 * @param type
	 *          combination behavior AND,OR,NAND,NOR
	 */
	public FileFilter(int type) {
		this.type = type & 0x03;
	}

	/**
	 * CONSTRUCTS a default composite filter which ANDs it's tests.
	 */
	public FileFilter() {
		this.type = AND;
	}

	/**
	 * Performs the test(s) as required
	 */
	public boolean accept(File dir, String name) {
		boolean flag = true;
		if ((type & OR) > 0) {
			// OR condition, any acceptance is OK
			flag = false;
			for (FilenameFilter ff : filters) {
				if (ff.accept(dir, name)) {
					flag = true;
					break;
				}
			}

		} else {
			// AND condition, any failure rejects
			flag = true;
			for (FilenameFilter ff : filters) {
				if (!ff.accept(dir, name)) {
					flag = false;
					break;
				}
			}
		}
		// handle negation
		if ((type & NOT) > 0) {
			flag = !flag;
		}
		return flag;
	}

	/** Adds the provided FileFilter to the list of tests */
	public FileFilter addFileFilter(FilenameFilter filter) {
		if (!filters.contains(filter)) {
			filters.add(filter);
		}
		return this;
	}

	/**
	 * adds a check to see that the file exists
	 * dir/name exists and isFile
	 */
	public FileFilter testFileExists() {
		addFileFilter(new TestFileExist());
		return this;
	}

	private class TestFileExist
			implements FilenameFilter
	{
		public boolean accept(File dir, String name) {
			File f = new File(dir, name);
			return f.exists() && f.isFile();
		}
	}

	/**
	 * adds a check to see that the directory exists
	 * dir/name exists and isDirectory
	 */
	public FileFilter testDirExists() {
		addFileFilter(new TestDirExist());
		return this;
	}

	private class TestDirExist
			implements FilenameFilter
	{
		public boolean accept(File dir, String name) {
			File f = new File(dir, name);
			return f.exists() && f.isDirectory();
		}
	}

	/** adds a check to see that the file name starts with specified text */
	public FileFilter testNameStartsWith(String start) {
		addFileFilter(new TestNameStartsWith(start));
		return this;
	}

	private class TestNameStartsWith
			implements FilenameFilter
	{
		String text;

		public TestNameStartsWith(String text) {
			this.text = text;
		}

		public boolean accept(File dir, String name) {
			return name.startsWith(text);
		}
	}

	/** adds a check to see that the file name starts with specified text */
	public FileFilter testNameEndsWith(String start) {
		addFileFilter(new TestNameEndsWith(start));
		return this;
	}

	private class TestNameEndsWith
			implements FilenameFilter
	{
		String text;

		public TestNameEndsWith(String text) {
			this.text = text;
		}

		public boolean accept(File dir, String name) {
			return name.endsWith(text);
		}
	}

	/**
	 * adds a check to see that the file extension matches any in the given list
	 * (List is comma-separated)
	 */
	public FileFilter testExtension(String extList) {
		addFileFilter(new TestExtension(extList));
		return this;
	}

	private class TestExtension
			implements FilenameFilter
	{
		List<String> exts = new ArrayList<String>();

		public TestExtension(String text) {
			String[] sa = text.split("\\s*,*\\.*");
			for (String e : sa) {
				exts.add(e.toUpperCase());
			}
		}

		public boolean accept(File dir, String name) {
			int ix = name.lastIndexOf(".");
			return exts.contains(name.substring(ix + 1).toUpperCase());
		}
	}

}
