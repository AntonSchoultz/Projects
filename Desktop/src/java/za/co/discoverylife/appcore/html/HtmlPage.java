package za.co.discoverylife.appcore.html;

import java.io.File;

import za.co.discoverylife.appcore.util.FileUtil;

/**
 * Wraps an HTML page of tags.
 * 
 * @author Anton Schoultz 
 */
public class HtmlPage extends HtmlTag implements IStyle
{
  private HtmlTag head = null;
  private HtmlTag body = null;
  private HtmlStyle style = null;

  /** CONSTRUCT an HTML page */
  public HtmlPage()
  {
    super("HTML");
  }

  /** Write the html page to the provided file handle */
  public File writeToFile(File fHtml)
  {
    FileUtil.fileWrite(toString(), fHtml);
    return fHtml;
  }

  /** Return the page HEAD (create if required) */
  public HtmlTag head()
  {
    if ( head == null )
    {
      head = new HtmlTag("HEAD");
      add(head);
    }
    return head;
  }

  /** Return the page BODY (create if required) */
  public HtmlTag body()
  {
    if ( body == null )
    {
      body = new HtmlTag("BODY");
      add(body);
    }
    return body;
  }

  /** Return the page style (create if required) */
  public HtmlStyle style()
  {
    if ( style == null )
    {
      style = new HtmlStyle();
      head().add(style);
    }
    return style;
  }

  /** Sets the page title (creates head if needed) */
  public HtmlPage setTitle(String title)
  {
    head().add(new HtmlTag("TITLE").text(title));
    return this;
  }

  /** 
   * Sets a style specification to the page
   * creates head and/or style as needed
   *
   * @param id style key
   * @param values style value (excluding curly braces)
   * @return HtmlPage
   */
  public HtmlPage setStyle(String id, String values)
  {
    style().setAttribute(id, values);
    return this;
  }

  /** 
   * Adds a style specification to the page
   * creates head and/or style as needed
   * the attribute will be added to the style key.
   *
   * @param id style key
   * @param name attribute name for the style setting
   * @param value style value 
   * @return HtmlPage
   */
  public HtmlPage addStyle(String id, String name, String value)
  {
    style().appendAttribute(id, name, value);
    return this;
  }

  /**
   * Adds a java script 'import' into the head section.
   * @param scriptUrl URL to the script code
   * @return the html page
   */
  public HtmlPage addScriptImport(String scriptUrl)
  {
    HtmlTag script = new HtmlTag("script", ";");
    script.setAttribute("type", "text/javascript");
    script.setAttribute("src", scriptUrl);
    head().add(script);
    return this;
  }

  /** 
   * Adds a piece of script code in it's own tag
   * @return HtmlTag for the script tag just added. 
   */
  public HtmlTag addScript(String scriptCode)
  {
    HtmlTag script = new HtmlTag("script", scriptCode);
    add(script);
    return script;
  }

  /**
   * Sets up a default style.
   *
   * @return
   */
  public HtmlPage stylePastle()
  {
    setStyle("body", "font-family: sans-serif; font-size: 10px; color: #000000; background: white");
    addStyle("h1,h2,h3", FONT_FAMILY, FONT_FAMILY_SANS_SERIF);
    addStyle("h1,h2,h3", BACKGROUND_COLOR, "#ddddff");
    addStyle("h1,h2,h3", MARGIN_TOP, "5px");
    addStyle("h1,h2,h3", "padding", "3px");
    addStyle("h1", FONT_SIZE, FONT_SIZE_X_LARGE);
    addStyle("h2", FONT_SIZE, "large");
    addStyle("dt,dd,dl", MARGIN_TOP, "0");
    addStyle("dt", FONT_WEIGHT, FONT_WEIGHT_BOLD);
    addStyle("dt", MARGIN_TOP, "5px");
    addStyle("li", MARGIN_TOP, "-1.1em");
    addStyle("li", MARGIN_BOTTOM, "1.1em");
    setStyle("code", "font-weight: bold");
    setStyle("pre", "background-color:#ffffdd; border: 1px dashed red; padding: 4px");
    setStyle("i", "color: #ff0000");
    setStyle("table", "background: #cccccc; padding: 1px");
    setStyle("th", "background: #ffffcc;font-size: 12; white-space: nowrap;padding: 4px");
    setStyle("td", "padding-left: 4px; padding-right: 4px");
    setStyle("tr", "background: #CCFFCC");// light green
    setStyle("tr.odd", "background-color:#aaddaa;");// green
    setStyle("tr.even", "background-color:#ccffff;");// light cyan
    setStyle("tr.sp_hide", "display:none;");// used in sortpage.js
    setStyle("tr.sp_show", "display:table-row;");// used in sortpage.js
    return this;
  }

}
