package za.co.discoverylife.appcore.html;

/**
 * HTML Definition list wrapper
 */
public class HtmlDL extends HtmlTag
{
  /** CONSTRUCTS a definition list */
  public HtmlDL()
  {
    super("DL");
  }

  /** 
   * Defines a definition item
   * 
   * @param term The term being defined
   * @param tag HTML tag for the description 
   * @return this list
   */
  public HtmlDL defineItem(String term, HtmlTag tag)
  {
    return defineItem(term, tag.toString());
  }

  /**
   * Defines a simple definition item
   *
   * @param term The term being defined
   * @param description String for the description
   * @return this list
   */
  public HtmlDL defineItem(String term, String description)
  {
    add(new HtmlTag("DT", term));
    add(new HtmlTag("dd", description));
    return this;
  }

  /**
   * Defines a simple definition item
   *
   * @param term The term being defined
   * @param description String for the description
   * @return this list
   */
  public HtmlDL defineLinkedItem(String term, String url, String description)
  {
    add(new HtmlTag("DT", newLink(url, term, null).toString()));
    add(new HtmlTag("dd", description));
    return this;
  }

  /** 
   * Creates the DT item (with provided content)
   * then creates and returns the DD tag.
   *
   * @param term
   * @return DD tag
   */
  public HtmlTag defineTerm(String term)
  {
    add(new HtmlTag("DT", term));
    return add(new HtmlTag("dd"));
  }
}
