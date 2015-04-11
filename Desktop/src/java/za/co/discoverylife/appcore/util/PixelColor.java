package za.co.discoverylife.appcore.util;

import java.awt.Color;

/**
 * Provides various colour utility functions
 * 
 * @author anton11
 */
public class PixelColor
{
	private static final int FF = 0xFF;
	private static final int FFFF = 0xFFFF;
	private int alpha, red, green, blue;

	public PixelColor(int alpha, int red, int green, int blue) {
		super();
		this.alpha = alpha;
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public PixelColor(int argb) {
		alpha = (argb >> 24) & 0xFF;
		red = (argb >> 16) & 0xFF;
		green = (argb >> 8) & 0xFF;
		blue = (argb) & 0xFF;
	}

	public PixelColor(Color c) {
		this(c.getRGB());
	}

	public int getArgb() {
		return (alpha & FF) << 24 | (red & FF) << 16 | (green & FF) << 8 | (blue & FF);
	}

	public int mix(PixelColor B) {
		red = (red * alpha / FF) + (B.red * B.alpha * (FF - alpha) / FFFF);
		green = (green * alpha / FF) + (B.green * B.alpha * (FF - alpha) / FFFF);
		blue = (blue * alpha / FF) + (B.blue * B.alpha * (FF - alpha) / FFFF);
		alpha = alpha + (B.alpha * (FF - alpha) / FF);
		return B.getArgb();
	}

	public Color getColor() {
		return new Color(getArgb(), true);
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
	}

	public String toString() {
		return Integer.toHexString(alpha) + ","
				+ Integer.toHexString(red) + "," + Integer.toHexString(green) + "," + Integer.toHexString(blue)
				+ "[ " + Integer.toHexString(red) + " ]";
	}

}

