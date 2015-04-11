package za.co.discoverylife.appcore.html;

import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Wraps a style specification.
 * 
 * The attributes are abused to hold style id and values,
 * render is overridden to handle this.
 * 
 * @author Anton Schoultz 
 *
 */
public class HtmlStyle extends HtmlTag
{

	/** CONSTRUCTS a STYLE tag */
	public HtmlStyle() {
		super("STYLE");
	}

	/**
	 * append new value settings, rather than replace
	 */
	public void appendAttribute(String id, String name, String value) {
		if (attributes == null) {
			attributes = new TreeMap<String, String>();
		}
		String old = attributes.get(id);
		String attrib = name + ":" + value;
		if (old != null) {
			attrib = old + "; " + attrib;
		}
		setAttribute(id, attrib);
	}

	/**
	 * Override render because style attributes have a different syntax
	 */
	public void render(StringBuilder sbPage, String tab) {
		sbPage.append(tab).append("<").append(tag).append(">\r\n");
		if (attributes != null) {
			for (Entry<String, String> e : attributes.entrySet()) {
				sbPage.append(tab).append("  ").append(e.getKey()).append(" { ").append(e.getValue()).append("; }\r\n");
			}
		}
		sbPage.append(tab).append("</").append(tag).append(">\r\n");
	}

}
