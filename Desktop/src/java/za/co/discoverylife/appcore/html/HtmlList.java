/**
 * HtmlList.java
 */
package za.co.discoverylife.appcore.html;

/**
 *
 * @author Anton Schoultz (2013)
 */
public class HtmlList extends HtmlTag
{

  protected HtmlList(String tag)
  {
    super(tag);
  }

  public HtmlTag addItem(String text)
  {
    return add(new HtmlTag("li", text));
  }

  public HtmlTag addItem(HtmlTag tag)
  {
    return add(new HtmlTag("li", tag.toString()));
  }

  /** Creates link to as an item  */
  public HtmlTag addLinkItem(String url, String hotText, String hint)
  {
    return addItem(newLink(url, hotText, hint));
  }

}
