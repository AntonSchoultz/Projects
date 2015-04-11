package za.co.discoverylife.appcore.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import za.co.discoverylife.appcore.field.MetaFieldInfo;
import za.co.discoverylife.appcore.gui.buttons.GuiButton;
import za.co.discoverylife.appcore.gui.buttons.GuiButtonPanel;
import za.co.discoverylife.appcore.gui.screens.GuiCollectionScreen;
import za.co.discoverylife.appcore.gui.screens.GuiConfigScreen;
import za.co.discoverylife.appcore.gui.screens.GuiTabScreen;
import za.co.discoverylife.appcore.logging.LogManager;
import za.co.discoverylife.appcore.task.Task;

/**
 * Top-level screen for GUI forms
 * 
 * @author anton11
 */
public class GuiScreen
    extends GuiPanel implements ActionListener
{

  private static final long serialVersionUID = 6972597028194254605L;

  protected int indent = 0;
  protected GuiRow row;
  protected GuiTabScreen tabPane;
  protected int vertPos = 0;
  protected IGuiScreenCloser guiCloser = null;
  private boolean colorRows;

  /**
   * CONSTRUCTS a basic screen with vertical layout for GUI rows.
   */
  public GuiScreen(String name, String toolTip)
  {
    super();
    log = LogManager.getLogger(this.getClass());
    setName(name);
    setToolTipText(toolTip);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  }

  /** CONSTRUCTOR - default */
  public GuiScreen()
  {
    super();
    log = LogManager.getLogger(this.getClass());
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  }

  /** Remove this screen from the display / dialog */
  public void closeScreen()
  {
    if ( dialog != null )
    {
      dialog.closeGuiScreen(this);
    }
    super.closeScreen();
  }

  /** Pad down at the end of a page 
   * @return */
  public GuiRow endPage()
  {
    newRow();
    Dimension dim = GuiBase.getGuiBase().getScreenSize(100, 90);
    JPanel pad = new JPanel();
    pad.setMinimumSize(new Dimension(1, 1));
    pad.setMaximumSize(dim);
    pad.setPreferredSize(dim);
    row.add(pad);
    return row;
  }

  /** Adds and returns a row containing a tabbed panel */
  public GuiTabScreen addTabRow()
  {
    newRow();
    tabPane = new GuiTabScreen();
    row.add(tabPane);
    row.padAcross();
    return tabPane;
  }

  /**
   * Adds the supplied panel to the current tabbed pane, creates the tabbed pane
   * if needed.
   * 
   * @param panel
   */
  public void addPane(GuiScreen panel)
  {
    if ( tabPane == null )
    {
      addTabRow();
    }
    tabPane.addTabPanel(panel);
  }

  /** Apply vertical glue */
  public void padDown()
  {
    add(Box.createVerticalGlue());
    // xx = GuiBase.getGuiBase().height;
  }

  /** Apply vertical gap */
  public void padDown(int vGap)
  {
    if ( vGap > 0 )
    {
      add(Box.createVerticalStrut(vGap));
      vertPos += vGap;
    }
  }

  /** Create and add a new row */
  public GuiRow newRow()
  {
    return newRow(V_GAP);
  }

  /** Create and add a new row */
  public GuiRow newRow(int vgap)
  {
    row = new GuiRow();
    addRow(row, vgap);
    row.setParentGui(this);
    if ( indent > 0 )
    {
      row.padAcross(indent);
    }
    return row;
  }

  /**
   * Add a row with Label and a GUI edit field for the specified data object and
   * field name
   */
  public GuiRow addLabelAndFieldRow(Object object, String fieldName)
  {
    row = newRow(V_GAP);
    row.addLabelAndFieldRow(object, fieldName);
    return row;
  }

  /** Add a rows with Label and a GUI edit field for the given data object */
  public void addLabelsAndFieldsForObject(Object object)
  {
    Field[] flds = object.getClass().getDeclaredFields();
    for (Field f : flds)
    {
      // skip static fields and transient fields
      int mod = f.getModifiers();
      if ( Modifier.isTransient(mod) || Modifier.isStatic(mod) )
      {
        continue;
      }
      // only add fields marked with FieldInfo
      MetaFieldInfo fi = f.getAnnotation(MetaFieldInfo.class);
      if ( fi != null )
      {
        addLabelAndFieldRow(object, f.getName());
      }
    }
  }

  /**
   * Create and add a button Panel for the supplied model (Does not add the
   * buttons) see GuiButtonPanel.addButton & addAllbuttons
   * 
   * @param subject
   * @return
   */
  public GuiButtonPanel addButtonPanel(Object subject)
  {
    GuiButtonPanel bp = null;
    try
    {
      bp = new GuiButtonPanel(subject);
      addRow(bp, V_GAP + V_GAP);
      bp.setParentGui(this);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return bp;
  }

  /** Add a button whose action is defined in this screen */
  public GuiButton addLocalButton(String text, String hint, String iconRef, String actionKey, String value, int style)
  {
    GuiButton btn = new GuiButton(text, iconRef);
    btn.setActionCommand(actionKey);
    btn.setEnabled(true);
    btn.setDisplayStyle(style);
    btn.setToolTipText(hint);
    btn.setValue(value);
    row.addCell(btn);
    btn.addActionListener(this);
    return btn;
  }

  /**
   * Implements ActionListener to handle buttons by firing method given by
   * actionCommand in it's own thread. This is done so that the GUI thread is
   * not parked waiting for CMD to finish.
   */
  public void actionPerformed(ActionEvent e)
  {
    try
    {
      GuiScreen screen = getScreen();
      screen.commit();
      String actnKey = e.getActionCommand();
      btnFired = (GuiButton) e.getSource();
      Task task = taskManager.getActionTask(actnKey, this, log);
      taskManager.doTaskList(TASK_SPAWN, task, screen);
    }
    catch (Exception e1)
    {
      log.error("Problem trying to action button " + e.getActionCommand(), e1);
    }
  }

  /**
   * Adds a selection list with a 'buddy' field for add/remove
   * 
   * @param selObj
   *          The object where the selection set (Strings) lives
   * @param selFieldName
   *          Name of the selection set variable
   * @param itemObject
   *          The object where the buddy field lives
   * @param itemFieldName
   *          The name of the buddy field
   * @param isTitled
   *          true=framed with title of selection field
   * @return
   */
  public GuiScreen addSelectionScreen(GuiCollectionScreen gcs)
  {
    row.addCell(gcs);
    adjustWidth(gcs, W_LABEL_TEXT + W_ICON);
    return gcs;
  }

  /** Add the provided row to this screen */
  public void addRow(GuiRow row)
  {
    addRow(row, V_GAP);
  }

  /** Add the provided row to this screen */
  public void addRow(GuiRow row, int vgap)
  {
    padDown(vgap);
    if ( colorRows )
    {
      row.setColors(null, bgColors[rowNo & 1]);
      rowNo++;
    }
    add(row);
    vertPos += V_TEXT_HEIGHT;
    row.padAcross(H_GAP);
    this.row = row;
  }

  /** Draw a horizontal line on the screen */
  public void ruleLine(int vgap)
  {
    newRow(vgap);// newRow(V_BLOCK);
    JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
    //sep.setForeground(Color.RED);
    //sep.setBackground(Color.BLUE);
    row.add(sep);
  }

  int rowNo = 0;
  Color[] bgColors = {new Color(0xDD, 0xFF, 0xDD), new Color(0xDD, 0xFF, 0xFF)};

  protected HashMap<String, List<String>> optionsMap = new HashMap<String, List<String>>();

  /** Used to get options for a drop-down screen */
  @SuppressWarnings({"rawtypes", "unchecked"})
  public List<String> getOptions(String listName)
  {
    List<String> rtn = optionsMap.get(listName);
    if ( rtn != null )
    {
      return rtn;
    }
    String name = "get" + listName + "List";
    Class[] ca = new Class[0];
    Object[] oa = new Object[0];
    Object subject = getScreen();
    if ( subject instanceof GuiConfigScreen )
    {
      subject = ((GuiConfigScreen) subject).getObject();
    }
    try
    {
      Method getOptsMeth = subject.getClass().getMethod(name, ca);
      rtn = (List<String>) getOptsMeth.invoke(subject, oa);
      optionsMap.put(listName, rtn);
    }
    catch (Exception e)
    {
      log.error("GUIDropDownField could not load options for " + listName + " in " + this.getClass().getName(), e);
      ArrayList<String> al = new ArrayList<String>();
      al.add("No Options found for " + listName);
      e.printStackTrace();
      return al;
    }
    return rtn;
  }

  /** Add a list of options for a named drop-down */
  public void addOptionsList(String name, String... values)
  {
    List<String> rtn = optionsMap.get(name);
    if ( rtn == null )
    {
      rtn = new ArrayList<String>();
      optionsMap.put(name, rtn);
    }
    for (String v : values)
    {
      rtn.add(v);
    }
  }

  /** Returns the indent amount */
  public int getIndent()
  {
    return indent;
  }

  /** Sets the indent value */
  public void setIndent(int indent)
  {
    this.indent = indent;
  }

  /** Close this screen */
  public void close()
  {
    if ( this.closeable )
    {
      removeAll();
      setVisible(false);
      if ( guiCloser != null )
      {
        guiCloser.closeGuiScreen(this);
      }
    }
  }

  /**
   * Display a confirmation dialogue with provided message and return the
   * confirm/deny result
   */
  public boolean confirm(String title, String message)
  {
    int n = JOptionPane.showConfirmDialog(this.getScreen(),
        message, title,
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);
    return n == JOptionPane.YES_OPTION;
  }

  public IGuiScreenCloser getGuiCloser()
  {
    return guiCloser;
  }

  public void setGuiCloser(IGuiScreenCloser guiCloser)
  {
    this.guiCloser = guiCloser;
  }

  public boolean isColorRows()
  {
    return colorRows;
  }

  public void setColorRows(boolean colorRows)
  {
    this.colorRows = colorRows;
    rowNo = 0;
  }

}
