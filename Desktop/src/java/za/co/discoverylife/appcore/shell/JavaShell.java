package za.co.discoverylife.appcore.shell;

/**
 * Provides functionality to call a java process on the command line.
 * 
 * @author Anton Schoultz - 21 Jan 2010
 */
public abstract class JavaShell
		extends BaseShell {

	// SYSTEM PROPERTIES - some useful ones ? :)
	// java.home = "C:\Program Files\Java\jre1.5.0"
	// sun.boot.library.path = "C:\Program Files\Java\jre1.5.0\bin"
	// user.home=d:\Users Documents\anton11
	// user.name=anton11
	// sun.cpu.endian=little
	
	// Too small initial heap for new size specified (-Xmn128M -Xms128M -Xmx512M -Xss1024k)

	protected static String jreHome;
	protected static String jreBin;
	protected static String jdkHome;
	protected static String jdkBin;

	protected static int minHeapMb = 320;// -Xms 		 128	
	protected static int maxHeapMb = 1024;//  -Xmx	 512
	protected static int edenHeapMb = 128;// -Xmn		 128
	protected static int stackSizeKb = 1024;//  -Xss	1024

	/**
	 * CONSTRUCTs a JavaShell helper to call java programs on the command line.
	 * 
	 * @param refName
	 *          Reference to prepend to the logging output.
	 * @param JDK_Home
	 *          String specifying the JDK home to be used.
	 */
	public JavaShell() {
		super("JavaShell");
	}

	/** Sets JDK home */
	public static void setJdkHome(String JDK_Home) {
		if (JDK_Home == null) {
			jdkHome = System.getProperty("java.home");
		} else {
			jdkHome = JDK_Home;
		}
		jreHome = jdkHome + FILE_SEPARATOR + "jre";
		jreBin = jreHome + FILE_SEPARATOR + "bin";
		jdkBin = jdkHome + FILE_SEPARATOR + "bin";
	}

	/** sets the stack size for the JVM in Mb (min & max) */
	public static void setHeapSizes(int minMb, int maxMb) {
		// math functions used to ensure that maxHeap>=minHeap
		minHeapMb = Math.min(minMb, maxMb);
		maxHeapMb = Math.max(minMb, maxMb);
	}

	/** sets the EdenGen size for the JVM in Mb */
	public static void setEdenSize(int edenMb) {
		edenHeapMb = edenMb;
	}

	/** sets the stack size for the JVM in Kb */
	public static void setStackSize(int sizeKb) {
		stackSizeKb = sizeKb;
	}

	/** add a named jar (in JDK/lib) to the CLASSPATH */
	public void addJdkLib(String jarName) {
		addClassPath(jdkHome + FILE_SEPARATOR + "lib" + FILE_SEPARATOR + jarName);
	}

	/** Returns the effective command line */
	public String getCmdString() {
		// validate heap settings
		int rqdMin = edenHeapMb * 3 / 2;
		if(minHeapMb <rqdMin ){
			minHeapMb = rqdMin;
		}
		// set up command line
		addEnvParameter("JAVA_HOME", jdkHome);
		setTimeStamps();
		StringBuilder sb = new StringBuilder();
		sb.append(jdkHome).append("/bin/");
		sb.append("java ");
		if (edenHeapMb > 0) {
			sb.append(" -Xmn").append(edenHeapMb).append("M");
		}
		if (minHeapMb > 0) {
			sb.append(" -Xms").append(minHeapMb).append("M");
		}
		if (maxHeapMb > 0) {
			sb.append(" -Xmx").append(maxHeapMb).append("M");
		}
		if (stackSizeKb > 0) {
			sb.append(" -Xss").append(stackSizeKb).append("k");
		}
		// sb.append(" -DYMD=").append(YMD);
		// sb.append(" -Dhms=").append(hms);
		setParam("YMD", YMD);
		setParam("hms", hms);

		for (String k : mapParams.keySet()) {
			String v = mapParams.get(k);
			sb.append(" -D").append(k).append("=").append(v);
		}
		if (sbClassPath.length() > 0) {
			sb.append(" -cp ").append(sbClassPath.toString());
		}
		for (String s : args) {
			sb.append(' ');
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * @return the javaHome
	 */
	public String getJreHome() {
		return jreHome;
	}

	/**
	 * @return the javaBin
	 */
	public String getJreBin() {
		return jreBin;
	}

	/**
	 * @return the jdkHome
	 */
	public static String getJdkHome() {
		return jdkHome;
	}

	/**
	 * @return the jdkBin
	 */
	public String getJdkBin() {
		return jdkBin;
	}

}
