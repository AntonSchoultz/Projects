package za.co.discoverylife.appcore.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import za.co.discoverylife.appcore.field.FieldAccessException;
import za.co.discoverylife.appcore.field.FieldAccessor;
import za.co.discoverylife.appcore.field.IFieldTypes;
import za.co.discoverylife.appcore.gui.buttons.GuiButton;
import za.co.discoverylife.appcore.gui.screens.GuiCollectionScreen;

/**
 * Holds a GUI version of a field and handles editing of the data.
 * 
 * @author anton11
 */
public class GuiField extends GuiPanel implements IFieldTypes, ActionListener, KeyListener
{
  private static final long serialVersionUID = 1834505881891335881L;

  private static final String ICON_FILE = "file";
  private static final String ICON_FOLDER = "folder";

  protected FieldAccessor fieldAccess;// has own local string version of the
                                      // field's value
  protected int fieldType;
  protected String listName = "";
  protected boolean selection = false;
  protected GuiScreen screen;
  protected JComponent comp;
  // drop-down
  protected DefaultComboBoxModel dropDownModel;
  protected JComboBox dropDown;
  protected Method onSelect = null;
  // file (wrapped in it's own GuiRow panel)
  protected JFileChooser fc = null;
  protected GuiButton fBtn = null;
  protected JTextField fText = null;
  // sub-screen
  protected GuiScreen subScreen = null;
  protected KeyListener keyListner = null;

  //
  int w;
  int h;
  int cols;
  int rows;
  String fieldName;
  String hint;

  /** CONSTRUCT a GUI component to edit the named field of the provided object */
  public GuiField(GuiPanel parentPanel, Object object, String fieldName) throws FieldAccessException
  {
    super();
    this.fieldAccess = new FieldAccessor(object, fieldName);
    this.guiparent = parentPanel;
    screen = parentPanel.getScreen();
    createComponent();
  }

  /** Constructs a GUI component to edit the field via the provided accessor */
  public GuiField(GuiPanel parentPanel, FieldAccessor fieldAccess)
  {
    super();
    this.fieldAccess = fieldAccess;
    this.guiparent = parentPanel;
    screen = parentPanel.getScreen();
    createComponent();
  }

  /** Creates an appropriate GUI component with which to edit this field */
  private void createComponent()
  {
    fieldType = fieldAccess.getFieldType();
    cols = fieldAccess.getSize();
    rows = Math.max(1, fieldAccess.getDropRows());
    w = cols * W_CHAR;//W_TEXT;
    h = rows * V_TEXT_HEIGHT;
    //wr("Create component "+fieldAccess.getFieldName()+", size="+cols+", w="+w+", h="+h);
    // -- handle drop-down (listName given)
    listName = fieldAccess.getListName();
    if ( listName != null && listName.trim().length() > 0 )
    {
      handleDropDownList();
    }
    else
    {
      handleBasicField();
    }
    // apply width & height to component
    setCompSize();
    // apply read only
    if ( fieldAccess.isReadOnly() )
    {
      comp.setEnabled(false);
    }
    undo();// load data value into the GUI component
  }

  /**
   * Create edit field for 'basic' fields (basic=fields that can be represented
   * by a single item, usually a string)
   */
  public void handleBasicField()
  {
    fieldName = fieldAccess.getFieldName();
    hint = fieldAccess.getHint();
    switch (fieldType)
    {
      case TYPE_DATE :
        handleDate();
        break;
      case TYPE_COLLECTION :
        handleCollection();
        break;
      case TYPE_OBJECT :
        handleObject();
        break;
      case TYPE_STRING :
        handleString();
        break;
      case TYPE_BOOLEAN :
        handleBoolean();
        break;
      case TYPE_FILE :
        handleFile();
        break;
      default :
        handleNumeric();
        break;
    }
    comp.setToolTipText(hint);
    setCompSize();
  }

  /** prepare components for a Date field */
  public void handleDate()
  {
    comp = new GuiDate();
  }

  /** prepare components for a file as a path specification and a browse button */
  public void handleFile()
  {
    comp = new GuiRow();
    fText = new JTextField();
    fText.setColumns(cols);
    if ( fieldAccess.isReadOnly() )
    {
      fText.setEditable(false);
    }
    setCompSize(fText, w, h); //adjustWidth(fText, w);
    comp.add(fText);
    ImageIcon icon = GuiIconManager.getIcon(fieldAccess.isFolder() ? ICON_FOLDER : ICON_FILE);
    fBtn = new GuiButton("Browse", icon);
    fBtn.setAsMiniButton();
    // fBtn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
    fBtn.setToolTipText("Browse to :" + hint);
    adjustWidth(fBtn, W_ICON);
    comp.add(fBtn);
    fBtn.addActionListener(this);
    w = w + W_ICON + H_GAP;
  }

  /** prepare components for a numeric field as a short text field */
  public void handleNumeric()
  {
    JTextField tf = new JTextField();
    tf.setColumns(cols);
    comp = tf;
  }

  /** prepare components for a boolean as a check-box */
  public void handleBoolean()
  {
    // treat as check-box
    w = W_CHECK_BOX;
    JCheckBox cb;
    comp = cb = new JCheckBox();
    cb.setBorder(null);// no white space around the check box
  }

  /** 
   * prepare components for a String field
   * either as a simple text lien, or as a text area depending on meta information.
   */
  public void handleString()
  {
    if ( fieldAccess.getFieldName().toUpperCase().indexOf("PASSWORD") >= 0 )
    {
      JPasswordField pf = new JPasswordField();
      comp = pf;
    }
    else
    {
      if ( fieldAccess.isReadOnly() )
      {
        comp = new JLabel("Read-Only String");
      }
      else
      {
        if ( rows <= 1 )
        {
          JTextField tf = new JTextField(fieldAccess.getStringFromField());
          comp = tf;
        }
        else
        {
          JTextArea ta = new JTextArea(rows, W_TEXT_CHARS);
          ta.setColumns(cols);
          ta.setRows(rows);
          ta.setLineWrap(true);
          ta.setWrapStyleWord(true);
          ta.setText(fieldAccess.getStringFromField());
          ta.setBorder(BorderFactory.createLineBorder(Color.BLACK));
          comp = ta;
        }
      }
    }
  }

  /** Handle a compound object, by using a sub-screen with multiple fields */
  public void handleObject()
  {
    subScreen = new GuiScreen(fieldName, hint);
    subScreen.setParentGui(screen);
    subScreen.titledBorder(fieldAccess.getLabel(), 1);
    Object subObject = fieldAccess.getFieldObject();
    if ( subObject == null )
    {
      throw new NullPointerException(fieldAccess.toString());
    }
    subScreen.addLabelsAndFieldsForObject(subObject);
    comp = subScreen;
    w = W_LABEL_TEXT + W_ICON;
  }

  /** prepare components for a collection by using a sub-screen for collections */
  public void handleCollection()
  {
    subScreen = new GuiCollectionScreen(screen, fieldAccess, null, "", true);
    subScreen.titledBorder(fieldAccess.getLabel(), 1);
    comp = subScreen;
    w = W_LABEL_TEXT + W_ICON;
  }

  /** prepare components for a selection field by using a drop-down */
  public void handleDropDownList()
  {
    selection = true;
    dropDownModel = new DefaultComboBoxModel();
    dropDown = new JComboBox(dropDownModel);
    dropDown.addActionListener(this);
    int n = fieldAccess.getDropRows();
    if ( n > 0 )
    {
      dropDown.setMaximumRowCount(n);
    }
    comp = dropDown;
    h = V_TEXT_HEIGHT;
    populateDropDown();
    // check if screen has a onSelectXxxxx(String) method for this drop-down if
    // so, take note of it.
    if ( onSelect == null )
    {
      String selectMethodName = "onSelect" + fieldAccess.getFieldNameUpperFirst();
      try
      {
        Class<?>[] ca = new Class[1];
        ca[0] = String.class;
        onSelect = screen.getClass().getMethod(selectMethodName, ca);
      }
      catch (Exception e1)
      {
        onSelect = null;
      }
    }
  }

  /** sets component size */
  private void setCompSize()
  {
    Dimension dim = new Dimension(w, h);
    comp.setPreferredSize(dim);
    comp.setMinimumSize(dim);
    comp.setMaximumSize(dim);
  }

  /** sets component size */
  private void setCompSize(JComponent comp, int w, int h)
  {
    Dimension dim = new Dimension(w, h);
    comp.setPreferredSize(dim);
    comp.setMinimumSize(dim);
    comp.setMaximumSize(dim);
  }

  public void keyTyped(KeyEvent e)
  {
    if ( e.getID() == KeyEvent.KEY_TYPED )
    {
      int keyCode = e.getKeyChar();
      if ( keyCode == KeyEvent.VK_ENTER )
      {
        if ( screen == null )
        {
          return;
        }
        Object[] oa = new Object[0];
        Class<?>[] ca = new Class[0];
        try
        {
          Method m = screen.getClass().getMethod("doENTER", ca);
          commit();
          m.invoke(screen, oa);
        }
        catch (Exception e1)
        {
          e1.printStackTrace();
        }
      }
    }
  }

  public void keyPressed(KeyEvent e)
  {
  }

  public void keyReleased(KeyEvent e)
  {
  }

  /**
   * Re-loads the drop-down list of options from the screen's list
   */
  private void populateDropDown()
  {
    if ( listName != null && listName.trim().length() > 0 )
    {
      selection = true;
      List<String> optnList = screen.getOptions(listName);
      Collection<ComboItem> ciList = new TreeSet<GuiField.ComboItem>();
      dropDownModel.removeAllElements();
      for (String optn : optnList)
      {
        ciList.add(new ComboItem(optn));
      }
      for (ComboItem ci : ciList)
      {
        dropDownModel.addElement(ci);
      }
    }
    dropDown.setEnabled(dropDownModel.getSize() > 0);
  }

  /**
   * Called when
   * <dl>
   * <dt>drop-down selection
   * <dd>fire screen's onSelectXxxx method if there is one
   * <dt>file/folder button
   * <dd>Pop-up file chooser
   * </dl>
   */
  public void actionPerformed(ActionEvent actionEvent)
  {
    if ( actionEvent.getSource() == dropDown )
    {
      if ( screen == null )
        return;
      actionDropDown();
    }
    if ( actionEvent.getSource() == fBtn )
    {
      actionFileFolderSelect();
    }
  }

  /** handle changes to drop-down */
  protected void actionDropDown()
  {
    if ( onSelect != null )
    {
      ComboItem selectedItem = (ComboItem) dropDown.getSelectedItem();
      if ( selectedItem != null )
      {
        Object[] oa = new Object[]{selectedItem.getStringValue()};
        try
        {
          onSelect.invoke(screen, oa);
        }
        catch (Exception e1)
        {
          e1.printStackTrace();
        }
      }
    }
  }

  /** handle the file browse button click by displaying file browser and setting file as selected */
  protected void actionFileFolderSelect()
  {
    if ( fc == null )
    {
      fc = new JFileChooser();
      fc.setName(fieldAccess.getFieldName());
      fc.setDialogTitle("Select " + fieldAccess.getLabel());
      fc.setFileSelectionMode(fieldAccess.isFolder() ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
    }
    File f = new File(fText.getText());
    fc.setCurrentDirectory(f.getParentFile());
    fc.setSelectedFile(f);
    if ( fc.showOpenDialog(comp) == JFileChooser.APPROVE_OPTION )
    {
      f = fc.getSelectedFile();
      if ( fieldAccess.isFolder() && f.isFile() )
      {
        f = f.getParentFile();
      }
      String edtVal = f.getAbsolutePath();
      fText.setText(edtVal);
    }
  }

  /** Commit the edited value to the object */
  public void commit()
  {
    String val = "";
    switch (fieldType)
    {
      case TYPE_COLLECTION :
      case TYPE_OBJECT :
        if ( subScreen != null )
        {
          subScreen.commit();
        }
        return;
      case TYPE_BOOLEAN :
        val = ((JCheckBox) comp).isSelected() ? "true" : "false";
        break;
      case TYPE_FILE :
        val = fText.getText();
        break;
      case TYPE_DATE :
        val = ((GuiDate) comp).toString();
        break;
      default :
        // the rest are numerics / strings
        if ( selection )
        {
          ComboItem ci = (ComboItem) dropDown.getSelectedItem();
          if ( ci != null )
          {
            val = ci.getStringValue();
          }
        }
        else
        {
          if ( fieldAccess.isReadOnly() )
          {
            val = ((JLabel) comp).getText();
          }
          else
          {
            val = ((JTextComponent) comp).getText();
          }
        }
        break;
    }
    // commits the value to the object
    fieldAccess.setFieldFromString(val);
  }

  public void setFieldFromString(String val)
  {
    fieldAccess.setFieldFromString(val);
    undo();
  }

  public String getFieldAsString()
  {
    return fieldAccess.getEditedValue();
  }

  /** set edited value from the object */
  public void undo()
  {
    fieldAccess.undo();
    if ( fBtn != null )
    {
      fBtn.undo();
      fBtn.setEnabled(true);
    }
    copyEditToGUI();
  }

  /** sets the edited value from the field's initial/default value */
  public void reset()
  {
    fieldAccess.reset();// objects value is set to initial value (default)
    copyEditToGUI();
  }

  /** Updates the display to reflect the field's value */
  private void copyEditToGUI()
  {
    Object valObj = fieldAccess.getEditedValueAsObject();
    switch (fieldType)
    {
      case TYPE_COLLECTION :
      case TYPE_OBJECT :
        break;
      case TYPE_BOOLEAN :
        // treat as check-box
        ((JCheckBox) comp).setSelected((Boolean) valObj);
        break;
      case TYPE_FILE :
        fText.setText(valObj.toString());
        break;
      case TYPE_DATE :
        ((GuiDate) comp).setToDate((Date) valObj);
        break;
      default :
        if ( selection )
        {
          populateDropDown();
          int mx = dropDown.getItemCount();
          for (int ix = 0; ix < mx; ix++)
          {
            ComboItem ci = (ComboItem) dropDown.getItemAt(ix);
            if ( ci.getStringValue().equalsIgnoreCase(valObj.toString()) )
            {
              dropDown.setSelectedIndex(ix);
              break;
            }
          }
        }
        else
        {
          if ( fieldAccess.isReadOnly() )
          {
            JLabel jl = (JLabel) comp;
            jl.setText(valObj.toString());
            jl.setEnabled(true);
          }
          else
          {
            ((JTextComponent) comp).setText(valObj.toString());
          }
        }
        break;
    }
  }
  public JComponent getComp()
  {
    return comp;
  }

  public String getLabel()
  {
    return fieldAccess.getLabel();
  }

  public String getToolTip()
  {
    return fieldAccess.getHint();
  }

  /**
   * Returns true if the field is a composite OBJECT - will be in it's own
   * sub-screen
   */
  public boolean isSubScreen()
  {
    return subScreen != null;
  }

  @Override
  public String toString()
  {
    return "GuiField [fa.Name=" + fieldAccess.getFieldName() + ", fa.EditValue=" + fieldAccess.getEditedValue() + "]";
  }

  /**
   * Converts a String[] to an enumerated list suitable for a drop down field
   * @param ary String[] of descriptions
   * @return List<String> of 'n=Description'...
   */
  public static List<String> arrayToDropList(String... ary)
  {
    List<String> list = new ArrayList<String>();
    int l = ary.length;
    for (int i = 0; i < l; i++)
    {
      list.add(String.valueOf(i) + "=" + ary[i]);
    }
    return list;
  }

  /** Holds value and description for a drop-down list */
  private class ComboItem implements Comparable<ComboItem>
  {
    private String stringValue;
    private String description;

    public ComboItem(String value)
    {
      String[] sa = value.split("=");
      stringValue = sa[0];
      if ( sa.length > 1 )
      {
        description = sa[1];
      }
      else
      {
        description = stringValue;
      }
    }

    public String toString()
    {
      return description;
    }

    public String getStringValue()
    {
      return stringValue;
    }

    public int compareTo(ComboItem o)
    {
      return description.compareTo(o.description);
    }

  }

}
