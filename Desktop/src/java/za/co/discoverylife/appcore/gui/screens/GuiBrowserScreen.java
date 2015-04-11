package za.co.discoverylife.appcore.gui.screens;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.PlainDocument;
import javax.swing.text.html.HTMLEditorKit;

import za.co.discoverylife.appcore.field.MetaFieldInfo;
import za.co.discoverylife.appcore.gui.GuiBase;
import za.co.discoverylife.appcore.gui.GuiLookAndFeel;
import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.gui.buttons.GuiButtonPanel;
import za.co.discoverylife.appcore.task.LinkTask;
import za.co.discoverylife.appcore.task.MetaTask;
import za.co.discoverylife.appcore.task.TaskManager;
import za.co.discoverylife.desktop.util.FileHelper;
import za.co.discoverylife.desktop.util.PixelColor;

/**
 * Provides a simple HTML browser.
 * 
 * @author anton11
 */
public class GuiBrowserScreen extends GuiScreen implements HyperlinkListener//, ISaveable // ,MouseListener
{
  private static final String BTN_FWD = "GuiBrowserScreen@forward";
  private static final String BTN_GO = "GuiBrowserScreen@go";
  private static final String BTN_BACK = "GuiBrowserScreen@back";
  private static final long serialVersionUID = 5312145458295825109L;
  private static final Font fixedFont = new Font("Monospaced", Font.PLAIN, 12);

  protected JEditorPane editorPane;
  protected JScrollPane scrollPane;

  @MetaFieldInfo(label = "Location", hint = "Location of page", isFolder = false)
  protected File fEdit = null;

  protected Dimension dim = GuiBase.getGuiBase().getScreenSize();

  private ArrayList<URL> history = new ArrayList<URL>();
  private int index = 0;
  TaskManager tm = TaskManager.getInstance();
  GuiButtonPanel bp;

  /**
   * CONSTRUCTS a text view screen
   * 
   * @param heading
   *          heading for the screen
   * @param fin
   *          text File to be viewed
   */
  public GuiBrowserScreen(String heading, File fin)
  {
    super(heading, fin.getAbsolutePath());
    TaskManager.getInstance().registerModel(getClass());
    fEdit = fin;
    String name = fin.getName();
    //setToolTipText(fin.getAbsolutePath());
    log.debug("Reading file " + fin.getAbsolutePath());
    String txt = FileHelper.fileRead(fin);
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

  /** Returns the text body from the editor */
  public String getText()
  {
    return editorPane.getText();
  }

  /** Returns true if the file name ends with .htm or .html */
  public boolean isHtml(String filename)
  {
    return filename.toLowerCase().endsWith(".htm") || filename.toLowerCase().endsWith(".html");
  }

  /** Sets the text to be displayed */
  public void setTextBody(String txt, boolean isHtml)
  {
    editorPane = new JEditorPane();
    if ( isHtml )
    {
      newRow();
      bp = addButtonPanel(this);
      bp.addButton("GuiBrowserScreen@home");
      bp.addButton(BTN_BACK);
      bp.addButton(BTN_FWD);
      bp.addField(this, "fEdit");
      bp.addButton(BTN_GO);
      bp.padAcross();
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
    newRow(4);
    editorPane.setBackground(PixelColor.hexToColor("ffffdd"));
    editorPane.setPreferredSize(dim);
    editorPane.setCaretPosition(0);// scroll to top
    scrollPane = new JScrollPane(editorPane);
    scrollPane.setPreferredSize(dim);
    row.addCell(scrollPane);
  }

  // override the default auto-refresh of all components so that only button panel is
  // updated. Otherwise the web page can flicker...
  public void undo()
  {
    bp.undo();// auto-update buttons (so that text/icon/both works)
  }

  @MetaTask(seqId = 10, label = "Back", hint = "Back to previous page", icon = "book_previous")
  public void back()
  {
    index = index - 2;
    jump();
  }

  @MetaTask(seqId = 11, label = "Forward", hint = "Forward to the next Page", icon = "book_next")
  public void forward()
  {
    jump();
  }

  @MetaTask(seqId = 12, label = "Home", hint = "Initial page", icon = "house")
  public void home()
  {
    index = 0;
    jump();
  }

  @MetaTask(seqId = 20, label = "Go", hint = "Initial page", icon = "book_open")
  public void go()
  {
    commit();
    try
    {
      URL u = fEdit.toURL();
      loadPage(u);
    }
    catch (MalformedURLException e)
    {
      log.error("Problem browsing to " + fEdit.getAbsolutePath());
    }
  }

  /** go to a history page */
  private void jump()
  {
    loadPage(history.get(index));
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
      String target = url.toExternalForm();
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
      //editorPane.setToolTipText(url.getPath());
      if ( index >= history.size() )
      {
        history.add(url);
        index = history.size();
      }
      else
      {
        history.set(index++, url);
      }
      tm.setEnabled(BTN_BACK, index > 1);
      tm.setEnabled(BTN_FWD, (history.size() - index) > 0);
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

}
