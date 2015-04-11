package za.co.discoverylife.appcore.gui.screens;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.PlainDocument;
import javax.swing.text.html.HTMLEditorKit;

import za.co.discoverylife.appcore.gui.GuiLookAndFeel;
import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.task.LinkTask;
import za.co.discoverylife.appcore.task.MetaTask;
import za.co.discoverylife.appcore.task.TaskManager;
import za.co.discoverylife.desktop.util.FileHelper;
import za.co.discoverylife.desktop.util.PixelColor;

/**
 * Provides a simple view of a text or html file.
 * 
 * @author anton11
 */
public class GuiViewScreen extends GuiScreen implements HyperlinkListener
{

  private static final long serialVersionUID = 5312145458295825109L;
  private static final Font fixedFont = new Font("Monospaced", Font.PLAIN, 12);

  protected JEditorPane editorPane;
  protected File fEdit = null;
  private int pos = 0;
  private String txt;

  /**
   * CONSTRUCTS a text view screen
   * 
   * @param heading
   *          heading for the screen
   * @param fin
   *          text File to be viewed
   */
  public GuiViewScreen(String heading, File fin)
  {
    super(heading, fin.getAbsolutePath());
    fEdit = fin;
    String name = fin.getName();
    setToolTipText(fin.getAbsolutePath());
    log.debug("Reading file " + fin.getAbsolutePath());
    txt = FileHelper.fileRead(fin);
    log.debug("File size=" + txt.length());
    setTextBody(txt, isHtml(name));
    setName(heading);
    try
    {
      URL url = fin.toURL();
      loadPage(url);
    }
    catch (MalformedURLException e)
    {
      log.error("Problem converting file spec to an URL", e);
    }
  }

  public GuiViewScreen(String htmlText)
  {
    super("Report.htm", "temp.htm");
    txt = htmlText;
    fEdit = null;
    setTextBody(htmlText, true);
  }

  /**
   * CONSTRUCTS a text view screen
   * 
   * @param heading
   *          heading for the screen
   * @param resourceSpec
   *          name of resource file to be displayed
   * @throws IOException
   */
  public GuiViewScreen(String heading, String resourceSpec) throws IOException
  {
    super(heading, resourceSpec);
    setToolTipText(resourceSpec);
    String txt = FileHelper.resourceRead(resourceSpec);
    setTextBody(txt, isHtml(resourceSpec));
    setName(heading);
    loadResourcePage(resourceSpec);
  }

  /** Returns the text body from the editor */
  public String getText()
  {
    return editorPane.getText();
  }

  @MetaTask(seqId = 51, label = "Reload", hint = "Reload from file")
  public void reload()
  {
    if ( fEdit != null )
    {
      txt = FileHelper.fileRead(fEdit);
      log.report("Re-loaded File " + fEdit.getAbsolutePath());
    }
    editorPane.setText(txt);
  }

  /** Returns true if the file name ends with .htm or .html */
  public boolean isHtml(String filename)
  {
    return filename.toLowerCase().endsWith(".htm") || filename.toLowerCase().endsWith(".html");
  }

  /** Return File object which refers to the displayed file */
  public File getFile()
  {
    return fEdit;
  }

  /** Sets the text to be displayed */
  public void setTextBody(String txt, boolean isHtml)
  {
    editorPane = new JEditorPane();
    pos = 0;
    if ( isHtml )
    {
      editorPane.setContentType("text/html");
      editorPane.addHyperlinkListener(this);
      editorPane.setFont(GuiLookAndFeel.getPlainFont());
      editorPane.setEditorKit(new HTMLEditorKit());
    }
    else
    {
      editorPane.setContentType("text/plain");
      editorPane.setFont(fixedFont);
    }
    editorPane.setEditable(false);// links are inactive if file is editable
    editorPane.setText(txt);
    Document doc = editorPane.getDocument();
    if ( doc instanceof PlainDocument )
    {
      doc.putProperty(PlainDocument.tabSizeAttribute, new Integer(4));
    }
    editorPane.setBackground(PixelColor.hexToColor("ffffdd"));
    //editorPane.setCaretPosition(pos);// scroll to top
    add(editorPane);
    endPage();
  }

  @Override
  public void undo()
  {
    super.undo();
    editorPane.setCaretPosition(pos);
  }

  /**
   * Facilitates navigation of HTML links in the help page.
   */
  public void hyperlinkUpdate(HyperlinkEvent event)
  {
    if ( event.getEventType() == HyperlinkEvent.EventType.ACTIVATED )
    {
      URL uLink = event.getURL();
      loadPage(uLink);
    }
  }

  /** Load resource by reference */
  public void loadResourcePage(String ref)
  {
    URL url = getClass().getClassLoader().getResource(ref);
    loadPage(url);
  }

  /** Load resource by URL */
  public void loadPage(URL url)
  {
    try
    {
      String target = url.toString();
      int ix = target.indexOf(LinkTask.ACTION);
      if ( ix > 0 )
      {
        // Pseudo link to action
        LinkTask link = new LinkTask(target);
        link.setSource(getName());
        TaskManager.getInstance().doTask(link, TaskManager.TASK_SPAWN);
        return;
      }
      editorPane.setPage(url);
      editorPane.setToolTipText(url.getPath());
      if ( url.toString().endsWith(".log") )
      {
        highlightLinesWith(".java:", Color.YELLOW);
      }
    }
    catch (Exception e)
    {
      if ( url != null )
      {
        log.error("Problem loading page " + url.toString(), e);
      }
      else
      {
        log.debug("loadPage(null) could not be handled.");
      }
    }
  }

  public void highlightLinesWith(String key, Color c)
  {
    /** Allows highlighting of keywords/phrases */
    Highlighter highlighter = editorPane.getHighlighter();
    highlighter.removeAllHighlights();
    DefaultHighlighter.DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(c);
    int bx = 0;
    pos = 0;
    String txt = editorPane.getText().replace('\r', ' ');
    editorPane.setText(txt);
    if ( key != null && key.length() > 0 )
    {
      while (bx < txt.length())
      {
        int ex = txt.indexOf('\n', bx);
        if ( ex > 0 )
        {
          int ix = txt.indexOf(key, bx);
          if ( ix >= 0 && ix < ex )
          {
            if ( pos == 0 )
            {
              pos = ex;
            }
            try
            {
              highlighter.addHighlight(bx, ex, painter);
            }
            catch (BadLocationException e)
            {
            }
          }
        }
        bx = ex + 1;
      }
    }
  }

  /** Allows highlighting of keywords/phrases */
  public void highlight(String key, Color c)
  {
    Highlighter highlighter = editorPane.getHighlighter();
    DefaultHighlighter.DefaultHighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(c);
    if ( key != null && key.length() > 0 )
    {
      String txt = editorPane.getText();
      int bx = txt.indexOf(key);
      int kl = key.length();
      int ex = bx + kl;
      if ( bx > 0 )
      {
        int ix = txt.indexOf('\r', ex + 2);
        if ( ix > ex )
        {
          ex = ix;
        }
        editorPane.select(bx, ex);
        editorPane.copy();// copy selected text to clip-board
        try
        {
          highlighter.addHighlight(bx, ex, painter);
        }
        catch (BadLocationException e)
        {
        }
        editorPane.setCaretPosition(ex + 1);
      }
    }
  }

  /**
   * Turn off all highlights
   */
  public void clearHighlights()
  {
    editorPane.getHighlighter().removeAllHighlights();
  }

}
