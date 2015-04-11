package za.co.discoverylife.appcore.gui.screens;

import java.awt.Font;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchEngine;

import za.co.discoverylife.appcore.field.MetaFieldInfo;
import za.co.discoverylife.appcore.gui.GuiLookAndFeel;
import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.gui.buttons.GuiButtonPanel;
import za.co.discoverylife.appcore.task.MetaTask;
import za.co.discoverylife.appcore.task.TaskManager;
import za.co.discoverylife.appcore.util.FileUtil;

/**
 * Provides a simple editor for text based files,
 * Includes syntax highlighting via RSyntaxTextArea.
 * Includes search/replace/highlight
 * Include save and re-load
 * Syntax highlight from http://fifesoft.com/rsyntaxtextarea/
 * 
 * @author anton11
 */
public class GuiEditScreen extends GuiScreen implements ISaveable
{
  private static final long serialVersionUID = 5312145458295825109L;
  private static Font fixedFont;// = new Font("Monospaced", Font.PLAIN, 10);
  protected RSyntaxTextArea editorPane;
  protected RTextScrollPane scrollPane;

  protected File fEdit = null;
  private boolean editable = false;

  boolean forward;

  @MetaFieldInfo(label = "Find:", hint = "Text to search for", width = 15)
  String find = "";

  @MetaFieldInfo(label = "Replace:", hint = "Replacement text", width = 15)
  String replace = "";

  @MetaFieldInfo(label = "Case", icon = "case", hint = "Case-sensitive?", initial = "false")
  boolean matchCase = false;

  @MetaFieldInfo(label = "Word", icon = "word", hint = "Whole word match?", initial = "false")
  boolean word = false;

  @MetaFieldInfo(label = "RegEx", icon = "expr", hint = "Regular Expresion matching?", initial = "false")
  boolean regex = false;

  /**
   * CONSTRUCTS a text view screen
   * 
   * @param heading
   *          heading for the screen
   * @param fin
   *          text File to be viewed
   */
  public GuiEditScreen(String heading, File fin)
  {
    this(heading, fin, false, false);
  }

  /**
   * CONSTRUCTS a text view screen
   * 
   * @param heading
   *          heading for the screen
   * @param fin
   *          text File to be viewed
   */
  public GuiEditScreen(String heading, File fin, boolean editable, boolean useToolBar)
  {
    super(heading, fin.getAbsolutePath());
    TaskManager.getInstance().registerModel(GuiEditScreen.class);
    fixedFont = new Font("Monospaced", Font.PLAIN, GuiLookAndFeel.getPlainFont().getSize());
    try
    {
      this.editable = editable;
      fEdit = fin;
      String name = fin.getName();
      String ext = "text";
      int dx = name.lastIndexOf(".");
      if ( dx > 0 )
      {
        ext = name.substring(dx + 1).toLowerCase();
      }
      // setToolTipText(fin.getAbsolutePath());
      log.debug("Reading file " + fin.getAbsolutePath());
      String txt = FileUtil.fileRead(fin);
      if ( useToolBar )
      {
        toolBar();
      }
      setTextBody(txt, ext);
      setName(heading);
      //setMinimumSize(editorPane.getPreferredSize());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @MetaTask(seqId = 50, label = "Save", hint = "Save the changes", icon = "script+#save")
  public void saveChanges()
  {
    if ( editable )
    {
      String txt = editorPane.getText();
      log.report("Saving File " + fEdit.getAbsolutePath());
      FileUtil.fileWrite(txt, fEdit);
    }
  }

  /** Returns the text body from the editor */
  public String getText()
  {
    return editorPane.getText();
  }

  @MetaTask(seqId = 51, label = "Reload", hint = "Reload from file", icon = "script+#refresh")
  public void reload()
  {
    String txt = FileUtil.fileRead(fEdit);
    editorPane.setText(txt);
    goToTop();
    log.report("Re-loaded File " + fEdit.getAbsolutePath());
  }

  /** Scroll to top of file */
  public void goToTop()
  {
    editorPane.setCaretPosition(0);// scroll to top
    scrollPane.getVerticalScrollBar().setValue(0);
  }

  /** Return File object which refers to the displayed file */
  public File getFile()
  {
    return fEdit;
  }

  public void load(File fin)
  {
    fEdit = fin;
    String txt = FileUtil.fileRead(fEdit);
    editorPane.setText(txt);
    goToTop();
    log.debug("Loaded File " + fEdit.getAbsolutePath());
  }

  /** Sets the text to be displayed */
  public void setTextBody(String txt, String ext)
  {
    editorPane = new RSyntaxTextArea();// new JEditorPane();

    if ( ext.equalsIgnoreCase("java") )
    {
      editorPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
    }
    if ( ext.equalsIgnoreCase("xml") )
    {
      editorPane.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
    }

    // /editorPane.setContentType("text/plain");
    editorPane.setFont(fixedFont);
    editorPane.setToolTipText("Mouse release after selecting will copy to clip-board");
    editorPane.setEditable(editable);
    editorPane.setText(txt);
    Document doc = editorPane.getDocument();
    if ( doc instanceof PlainDocument )
    {
      doc.putProperty(PlainDocument.tabSizeAttribute, new Integer(2));
    }
    scrollPane = new RTextScrollPane();
    scrollPane.setViewportView(editorPane);
    goToTop();
    newRow();
    row.addCell(scrollPane);
  }

  /** Constructs a button row on the screen to act as a find/replace tool bar */
  private void toolBar()
  {
    newRow();
    GuiButtonPanel bp = row.addButtonPanel(this);
    bp.addButton("GuiEditScreen@reload");
    bp.addButton("GuiEditScreen@saveChanges");
    bp.addLabelAndField(this, "find");
    bp.addToggleButton(this, "matchCase");
    bp.addToggleButton(this, "word");
    bp.addToggleButton(this, "regex");
    bp.padAcross(H_GAP);
    bp.addButton("GuiEditScreen@findPrev");
    bp.addButton("GuiEditScreen@findNext");
    bp.addButton("GuiEditScreen@markAll");
    bp.padAcross(H_GAP);
    bp.addLabelAndField(this, "replace");
    bp.padAcross(H_GAP);
    bp.addButton("GuiEditScreen@replace");
    bp.addButton("GuiEditScreen@replaceAll");
    bp.padAcross();
    row.padAcross();
  }

  /**
   * Finds the next matching text
   */
  @MetaTask(seqId = 11, label = "Next", hint = "Find Next match", icon = "zoom+#next")
  public void findNext()
  {
    find(true);
  }

  /**
   * Finds the previous matching text
   */
  @MetaTask(seqId = 12, label = "Prev", hint = "Find previous match", icon = "zoom+#previous")
  public void findPrev()
  {
    find(false);
  }

  /** Common find routine, forward=true searches for next, false searches previous */
  public void find(boolean forward)
  {
    commit();
    if ( find.length() == 0 )
    {
      return;
    }
    boolean found = SearchEngine.find(editorPane, find, forward, matchCase, word, regex);
    if ( !found )
    {
      JOptionPane.showMessageDialog(this, "Text not found");
    }
  }

  /**
   * Marks all occurrences of matching text
   */
  @MetaTask(seqId = 13, label = "MarkAll", hint = "Mark All occurences of the search text", icon = "lightbulb")
  public void markAll()
  {
    commit();
    if ( find.length() == 0 )
    {
      return;
    }
    if ( editorPane.markAll(find, matchCase, word, regex) <= 0 )
    {
      JOptionPane.showMessageDialog(this, "Text not found");
    }
  }

  /**
   * Performs a replace of matched text
   */
  @MetaTask(seqId = 20, label = "Replace", hint = "Replace text", icon = "zoom+#gear")
  public void replace()
  {
    try
    {
      commit();
      if ( find.length() == 0 )
      {
        return;
      }
      boolean found = SearchEngine.replace(editorPane, find, replace, forward, matchCase, word, regex);
      if ( !found )
      {
        JOptionPane.showMessageDialog(this, "Text not found");
      }
    }
    catch (Exception e)
    {
      log.error("Problem with Replace", e);
    }
  }

  /** Replaces all matched text */
  @MetaTask(seqId = 20, label = "ReplaceAll", hint = "Replace all text", icon = "zoom+#gear+#down")
  public void replaceAll()
  {
    try
    {
      commit();
      if ( find.length() == 0 )
      {
        return;
      }
      int found = SearchEngine.replaceAll(editorPane, find, replace, matchCase, word, regex);
      if ( found <= 0 )
      {
        JOptionPane.showMessageDialog(this, "Text not found");
      }
    }
    catch (Exception e)
    {
      log.error("Problem with Replace", e);
    }
  }

}
