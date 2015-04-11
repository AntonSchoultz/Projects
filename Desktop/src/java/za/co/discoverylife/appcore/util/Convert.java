package za.co.discoverylife.appcore.util;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class which provides various conversions.
 * 
 * @author anton11
 */
public class Convert
{

	private static final String ZEROES = "0000000000000000000000000000000000000000";

	/**
	 * Returns a string of '##:##:##' or '##h##m##s' for the time in milliseconds.
	 * 
	 * @param ms
	 *          Milliseconds to convert
	 * @param fmt
	 *          false -> '##:mm:ss', true -> '##h##m##s'
	 * @return
	 */
	public static String mSecToHMS(long ms, boolean fmt) {
		int s = (int) ((ms + 500l) / 1000l);
		int m = s / 60;
		s = (s % 60) + 100;
		int h = (m / 60) + 100;
		m = (m % 60) + 100;
		StringBuilder hms = new StringBuilder();
		hms.append(h).append(m).append(s);
		if (fmt) {
			hms.setCharAt(3, 'h');
			hms.setCharAt(6, 'm');
			hms.append('s');
			hms.insert(7, ' ');
			hms.insert(4, ' ');
		} else {
			hms.setCharAt(3, ':');
			hms.setCharAt(6, ':');
		}
		return hms.substring(1);
	}

	/**
	 * Returns the string value for the long provided, with leading zeroes.
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	public static String toFixedLength(long value, int places) {
		StringBuilder sb = new StringBuilder();
		if (places < 1 || places > ZEROES.length()) {
			places = ZEROES.length();
		}
		sb.append(ZEROES);
		sb.append(value);
		int ix = sb.length() - places;
		return sb.substring(ix);
	}

	/** Returns hh:mm:ss from mSec */
	public static String mSecToHMS(long ms) {
		return mSecToHMS(ms, false);
	}

	/** Return a {@link Color} for the string RRGGBB provided */
	public static Color hexToColor(String rrggbb) {
		int r = Integer.parseInt(rrggbb.substring(0, 2), 16);
		int g = Integer.parseInt(rrggbb.substring(2, 4), 16);
		int b = Integer.parseInt(rrggbb.substring(4), 16);
		return new Color(r, g, b);
	}

	private static final SimpleDateFormat sdfYmdHms = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	private static final SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyy/MM/dd");

	/** Returns 'YYYY/MM/DD hh:mm:ss' for the supplied long time */
	public static String formatLongAsDateTime(long tm) {
		if(tm<=0) return " ";
		Date dte = new Date( tm );
		return sdfYmdHms.format(dte);
	}

	/** Returns 'YYYY/MM/DD for the supplied long time */
	public static String formatLongAsDate(long tm) {
		if(tm<=0) return " ";
		Date dte = new Date( tm );
		return sdfYmd.format(dte);
	}

}
