package za.co.discoverylife.desktop.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a node/tag in an XML document
 * 
 * @author anton11
 */
public class XTag {
	private static final int WRAP_LEN = 90;
	private static final String NL = "\r\n";
	private static final String TAB = "  ";
	private String name;
	private Map<String, String> attributes;
	private List<XTag> children;
	private String body = null;

	private XTag parent = null;

	private List<XTag> instructions;

	/**
	 * CONSTRUCTS an XML tag with body text
	 * 
	 * @param tagName
	 * @param bodyText
	 */
	public XTag(String tagName, String bodyText) {
		body = bodyText == null ? null : bodyText.trim();
		name = tagName.trim();
		attributes = new TreeMap<String, String>();
		children = new ArrayList<XTag>();
		instructions = new ArrayList<XTag>();
	}

	/** CONSTRUCTOR - default constructor used for persistence */
	public XTag() {
		this(null, null);
	}

	/** CONSTRUCTS an XML tag with the provided tag name */
	public XTag(String tagName) {
		this(tagName, null);
	}

	public void removeChild(XTag xChild) {
		if (children != null && children.contains(xChild)) {
			children.remove(xChild);
		}
	}

	/** Comments out a tag by converting it into a comment tag */
	public void commentOut() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		appendAttributes("", sb);
		body = sb.toString();
		name = "!";
	}

	/** Returns true if this is the root tag/node (no parent) */
	public boolean isRoot() {
		return (parent == null);
	}

	/** Return true if this tag is empty (no name and no body) */
	public boolean isEmpty() {
		if (name != null && name.trim().length() > 0)
			return false;
		if (body != null
				&& body.replace('\r', ' ').replace('\n', ' ').trim().length() > 0)
			return false;
		return true;
	}

	/** Return true if this is an instruction tag */
	public boolean isInstruction() {
		return name.startsWith("?");
	}

	/** Add an instruction tag to this tag */
	public void addInstruction(XTag instruction) {
		instructions.add(instruction);
	}

	/**
	 * Return a string representation of this tag and it's children (returns XML
	 * string)
	 */
	public String toString() {
		return toXmlString("");
	}

	/**
	 * Return a string representation of this tag and it's children (returns XML
	 * string using the provided tab to format the string)
	 */
	public String toXmlString(String tab) {
		if (tab == null) {
			tab = "";
		}
		if (body != null) {
			if ("[".equals(name)) {
				return "<![CDATA[" + body + "]]>" + NL;
			}
			if ("!".equals(name)) {
				return tab + "<!-- " + body + " -->" + NL;
			}
			return body;
		}
		StringBuffer sb = new StringBuffer();
		for (XTag ti : instructions) {
			sb.append(ti.toXmlString(tab));
		}
		sb.append(tab);
		sb.append("<").append(name);
		appendAttributes(tab, sb);
		if (children.size() > 0) {
			sb.append(">").append(NL);
			XTag kid = null;
			for (XTag kd : children) {
				sb.append(kd.toXmlString(tab + TAB));
				kid = kd;
			}
			if (!kid.getName().equalsIgnoreCase("")
					&& !kid.getName().equalsIgnoreCase("[")) {
				sb.append(tab);
			}
			sb.append("</");
			sb.append(name);
			sb.append(">").append(NL);
		} else {
			if (name.startsWith("?")) {
				sb.append("?>").append(NL);
			} else {
				sb.append("/>").append(NL);
			}
		}
		return sb.toString();
	}

	private void appendAttributes(String tab, StringBuffer sb) {
		int bx = 0;
		if (attributes.size() > 0) {
			for (String attribName : attributes.keySet()) {
				String attrVal = attributes.get(attribName);
				if (attrVal != null) {
					if (((sb.length() - bx) + attribName.length() + attrVal
							.length()) > WRAP_LEN) {
						sb.append(NL).append(tab + TAB + TAB);
						bx = sb.length();
					}
					sb.append(" ");
					sb.append(attribName);
					sb.append("=\"");
					sb.append(attrVal);
					sb.append("\"");
				}
			}
		}
	}

	/** Adds the children from the supplied tag onto this tag */
	public void addAllChildren(XTag xt) {
		for (XTag xChild : xt.getChildren()) {
			addChild(xChild);
		}
	}

	/** Adds all attributes from the supplied tag */
	public void addAllAttributes(XTag xt) {
		attributes.putAll(xt.attributes);
	}

	/** Adds the provided remark as a child comment tag */
	public void addComment(String remark) {
		addChild(new XTag("!", remark));
	}

	/** Sets an attribute for this tag */
	public void setAttribute(String name, String value) {
		if (value != null) {
			attributes.put(name, value);
		}
	}

	/** Sets an attribute for this tag */
	public void setAttribute(String name, int value) {
		attributes.put(name, String.valueOf(value));
	}

	/** Returns an attribute value, null if not found */
	public String getAttribute(String name) {
		return attributes.get(name);
	}

	/** Returns an attribute value, or the supplied defaultValue if not found */
	public String getAttribute(String name, String defaultValue) {
		String value = attributes.get(name);
		if (value == null)
			value = defaultValue;
		return value;
	}

	/** Adds an XML tag as a child of this tag */
	public void addChild(XTag child) {
		children.add(child);
		child.parent = this;
	}

	/** Returns the tag name */
	public String getName() {
		return name;
	}

	/** Returns a map of this tag's attributes, name vs value */
	public Map<String, String> getAttributes() {
		return attributes;
	}

	/** Returns a list of this tag's direct children tags */
	public List<XTag> getChildren() {
		return children;
	}

	/** Returns a list of this tag's direct children tags with specified tagName */
	public List<XTag> getMatchingChildren(String tagName) {
		List<XTag> list = new ArrayList<XTag>();
		for (XTag xt : children) {
			if (xt.getName().compareTo(tagName) == 0) {
				list.add(xt);
			}
		}
		return list;
	}

	/**
	 * Returns the first child which has the requested tag name, null if not
	 * found
	 */
	public XTag findChild(String tagName) {
		for (XTag kid : children) {
			if (kid.getName().equals(tagName)) {
				return kid;
			}
		}
		return null;
	}

	/** Return the parent tag, null if none (aka root tag) */
	public XTag getParent() {
		return parent;
	}

	/** Sets the parent tag */
	public void setParent(XTag parent) {
		this.parent = parent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBody() {
		if (body == null) {
			StringBuilder sb = new StringBuilder();
			for (XTag kd : children) {
				sb.append(kd.toString());
			}
			return sb.toString();
		}
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public XTag locate(String query){
		return locate(this,query);
	}
	
	public static XTag locate(XTag xTop, String query) {
		if (xTop == null) {
			return null;
		}
		int ix = query.indexOf("/");
		if (ix >= 0) {
			return locate(locate(xTop, query.substring(0, ix)),
					query.substring(ix + 1));
		}
		return xTop.findChild(query);
	}

}
