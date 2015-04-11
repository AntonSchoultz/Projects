package za.co.discoverylife.appcore.xml;

public class XTagQuery {

	public static XTag locate(XTag xTop, String query) {
		if (xTop == null) {
			return null;
		}
		int ix = query.indexOf("/");
		if (ix >= 0) {
			//xSub = xTop.findChild(query.substring(0,ix));
			return locate(locate(xTop, query.substring(0, ix)), query.substring(ix + 1));
		}
		return xTop.findChild(query);
	}
}
