package za.co.discoverylife.appcore.html;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import za.co.discoverylife.appcore.table.DataRow;

/**
 * Represents an HTML tag and it's attributes
 * Handles rendering of tags as a string.
 * 
 * @author Anton Schoultz
 */
public class HtmlTag
{
  public static final String BLANK = "&nbsp;";

  protected String tag = null;
  protected String body = null;
  protected TreeMap<String, String> attributes = null;
  protected ArrayList<HtmlTag> children = null;

  protected HtmlTag parent = null;

  /** CONSTRUCTOR which accepts a tagname and body text */
  public HtmlTag(String tag, String body)
  {
    this.tag = tag;
    this.body = body;
    // HtmlTag htBody = add( new HtmlTag(tag));
    // htBody.body=body;
  }

  /** CONSTRUCTOR which accepts a tagname */
  public HtmlTag(String tag)
  {
    this.tag = tag;
    this.body = null;
  }

  /** Sets an attribute for this tag */
  public HtmlTag setAttribute(String name, int n)
  {
    return setAttribute(name, String.valueOf(n));
  }

  /** Sets an attribute for this tag */
  public HtmlTag setAttribute(String name, String value)
  {
    if ( attributes == null )
    {
      attributes = new TreeMap<String, String>();
    }
    attributes.put(name, value);
    return this;
  }

  /** Adds text body to this tag */
  public HtmlTag text(String text)
  {
    add(new HtmlTag(null, text));
    return this;
  }

  /** Adds text body to this tag */
  public HtmlTag text(long number)
  {
    add(new HtmlTag(null, String.valueOf(number)));
    return this;
  }

  /** Adds and returns the provided tag */
  public HtmlTag add(HtmlTag tag)
  {
    if ( children == null )
    {
      children = new ArrayList<HtmlTag>();
    }
    children.add(tag);
    tag.parent = this;
    return tag;
  }

  /** sets body text */
  public void setBody(String text)
  {
    body = text;
  }

  // =============================================================== builder
  // methods

  /** Returns parent if any (otherwise self) */
  public HtmlTag parent()
  {
    if ( parent != null )
    {
      return parent;
    }
    return this;
  }

  /** Create, add and return a new table */
  public HtmlTable table()
  {
    return (HtmlTable) add(new HtmlTable());
  }

  /** Create, add and return a new table with supplied header row */
  public HtmlTable table(DataRow hdrRow)
  {
    return (HtmlTable) add(new HtmlTable(hdrRow));
  }

  /** Create and add a heading at given level with provided text */
  public HtmlTag heading(int lvl, String text)
  {
    add(new HtmlTag("H" + lvl, text));
    return this;
  }

  /** Create, add and return header tag for given levl */
  public HtmlTag heading(int lvl)
  {
    return add(new HtmlTag("H" + lvl));
  }

  /** Create, add and return a CODE section */
  public HtmlTag code()
  {
    return add(new HtmlTag("CODE"));
  }

  /** Create, add and return a PRE- section */
  public HtmlTag pre()
  {
    return add(new HtmlTag("pre"));
  }

  /** Create, add and return a tag for the given tag and text body */
  public HtmlTag add(String tag, String text)
  {
    add(new HtmlTag(tag, text));
    return this;
  }

  /** Create, add and return BOLD */
  public HtmlTag bold()
  {
    return add(new HtmlTag("b"));
  }

  /** Add supplied test wrapped as BOLD */
  public HtmlTag bold(String text)
  {
    return add("b", text);
  }

  /** Create, add and return ITALIC */
  public HtmlTag italic()
  {
    return add(new HtmlTag("i"));
  }

  /** Add supplied test wrapped as ITALIC */
  public HtmlTag italic(String text)
  {
    return add("i", text);
  }

  /** Create, add and return UNDERLINED */
  public HtmlTag underline()
  {
    return add(new HtmlTag("u"));
  }

  /** Add supplied test wrapped as UNDERLINED */
  public HtmlTag underline(String text)
  {
    add(new HtmlTag("u", text));
    return this;
  }

  /** Add supplied test wrapped as BOLD ITALIC */
  public HtmlTag boldItalic(String text)
  {
    add(new HtmlTag("b").italic(text));
    return this;
  }

  /** Create, add and return a SPAN */
  public HtmlTag span()
  {
    return add(new HtmlTag("span"));
  }

  /** Create, add and return a SPAN which has the supplied hint set as title */
  public HtmlTag spanHint(String hint)
  {
    return add((new HtmlTag("span")).setAttribute("title", hint));
  }

  /** Create, add and return a SPAN which has a colour style set as given rgb */
  public HtmlTag spanColour(String rgb)
  {
    return add((new HtmlTag("span")).setAttribute("style", "{color: " + rgb + ";}"));
  }

  /** Create, add and return a Definition list */
  public HtmlDL DefList()
  {
    return (HtmlDL) add(new HtmlDL());
  }

  /** Create, add and return an ordered list */
  public HtmlOL OrderedList()
  {
    return (HtmlOL) add(new HtmlOL());
  }

  /** Create, add and return an ordered list */
  public HtmlUL UnOrderedList()
  {
    return (HtmlUL) add(new HtmlUL());
  }

  /** Creates link to target url, with provided hot-text. returns parent tag */
  public HtmlTag link(String url, String hotText)
  {
    return link(url, hotText, null);
  }

  /** Creates link to target url, with provided hot-text and hint. returns parent tag */
  public HtmlTag link(String url, String hotText, String hint)
  {
    add(newLink(url, hotText, hint));
    return this;
  }

  /** Creates link to target url, with provided hot-text and hint. returns parent tag */
  public HtmlTag newLink(String url, String hotText, String hint)
  {
    HtmlTag lnk = new HtmlTag("a");
    if ( hint != null )
    {
      lnk.span().setAttribute("title", hint).text(hotText);
    }
    else
    {
      lnk.text(hotText);
    }
    lnk.setAttribute("class", "btn");
    lnk.setAttribute("href", url);
    return lnk;
  }

  /** Creates link to target, returns the link tag (so user can add hot-text part) */
  public HtmlTag link(String tgt)
  {
    HtmlTag lnk = new HtmlTag("a");
    lnk.setAttribute("class", "btn");
    lnk.setAttribute("href", tgt);
    return add(lnk);
  }

  /** Create, add and return an anchor with the provided name (link target) */
  public HtmlTag anchor(String name)
  {
    HtmlTag lnk = new HtmlTag("A", BLANK);
    lnk.setAttribute("name", name);
    return add(lnk);
  }

  /** Insert a paragraph P */
  public HtmlTag p()
  {
    add(new HtmlTag(null, "\r\n<p>"));
    return this;
  }

  /** Insert a break BR */
  public HtmlTag br()
  {
    add(new HtmlTag(null, "\r\n<br>"));
    return this;
  }

  /** Insert a HR */
  public HtmlTag hr()
  {
    add(new HtmlTag(null, "\r\n<hr>"));
    return this;
  }

  // =============================================================== to string

  /** Render this tag and it's children as HTML string */
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    render(sb, "");
    return sb.toString();
  }

  /** Render this tag and it's children as HTML string */
  public void render(StringBuilder sbPage, String tab)
  {
    if ( tag == null || tag.length() == 0 )
    {
      sbPage.append(body);
      return;
    }
    boolean feed = tag.compareTo(tag.toUpperCase()) == 0;
    boolean nl = Character.isUpperCase(tag.charAt(0));
    if ( feed || nl )
    {
      sbPage.append("\r\n");
      sbPage.append(tab);
    }
    sbPage.append("<").append(tag);
    if ( attributes != null )
    {
      for (Entry<String, String> e : attributes.entrySet())
      {
        sbPage.append(" ").append(e.getKey());
        String v = e.getValue();
        if ( v != null )
        {
          sbPage.append("=\"").append(v).append("\"");
        }
      }
    }
    if ( body == null && children == null )
    {
      sbPage.append("/>");
      return;
    }
    sbPage.append(">");
    if ( body != null )
    {
      sbPage.append(body);
    }
    if ( children != null )
    {
      // sbPage.append("\r\n");
      for (HtmlTag tag : children)
      {
        tag.render(sbPage, tab + "  ");
      }
      if ( feed )
      {
        sbPage.append("\r\n");
        sbPage.append(tab);
      }
    }
    sbPage.append("</").append(tag).append(">");
  }

}
