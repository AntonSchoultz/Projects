package za.co.discoverylife.appcore.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import za.co.discoverylife.appcore.logging.ILogger;
import za.co.discoverylife.appcore.logging.LogManager;
import za.co.discoverylife.appcore.task.ITaskConstants;
import za.co.discoverylife.appcore.task.MetaTask;
import za.co.discoverylife.appcore.task.Task;
import za.co.discoverylife.appcore.task.TaskEntry;
import za.co.discoverylife.appcore.task.TaskManager;
import za.co.discoverylife.appcore.task.TaskNotFoundException;
/**
 * Creates a menu from the provided object by using annotations to
 * indicate the menu items (label,icon,hint and action method)
 * 
 * @author Anton Schoultz 
 */
public class GuiMenu
    extends MouseAdapter
    implements ActionListener, PropertyChangeListener, ITaskConstants
{
  public static final int ICON_ONLY = 0x02;
  public static final int TEXT_ONLY = 0x01;
  public static final int TEXT_AND_ICON = 0x03;
  public static final int DEFAULT = TEXT_ONLY;

  protected Object subject;
  protected ILogger log;
  protected boolean ispopup = false;
  protected JPopupMenu popupMenu;
  protected JMenu menu;
  protected TaskManager tm;
  protected int type = DEFAULT;
  protected HashMap<String, JMenuItem> map;
  protected char accel;
  protected Component triggerComponent;

  protected Object dataObject = null;

  protected String onClickMethod = null;

  /**
   * Constructs a new [sub]menu
   * 
   * @param subject The object which supports the menu items actions
   * @param logger logger for messages etc
   * @param title Text for the menu name, with accelerator character prefixed by an _
   * @param type Type of menu display ICON_ONLY/TEXT_ONLY/TEXT_AND_ICON
   * @param isPopup true for a pop-up, false for main menu
   * @param hint Tool tip text
   */
  public GuiMenu(Object subject, ILogger logger, String title, int type, boolean isPopup, String hint)
  {
    tm = TaskManager.getInstance();
    tm.registerModel(subject.getClass());
    map = new HashMap<String, JMenuItem>();
    String name = getMnemonic(title);
    this.ispopup = isPopup;
    if ( isPopup )
    {
      popupMenu = new JPopupMenu(name);
      popupMenu.setToolTipText(hint);
      popupMenu.setName(name);
    }
    else
    {
      menu = new JMenu(name);
      menu.setToolTipText(hint);
      if ( accel > 0 )
      {
        menu.setMnemonic(accel);
      }
      menu.setName(name);
      triggerComponent = menu;
    }
    if ( type > 0 )
    {
      this.type = type;
    }
    else
    {
      this.type = DEFAULT;
    }
    setSubject(subject);
    setLogger(logger);
  }

  /** 
   * Returns the Mnemonic (label) for the menu item,
   * also sets the accelerator key 
   * (which is specified by prefixing it with an underscore)
   *
   * @param name eg "Menu _Item"
   * @return Menu Item (and sets accelerator key to 'I')
   */
  private String getMnemonic(String name)
  {
    StringBuffer sb = new StringBuffer(name);
    accel = 0;
    int ix = name.indexOf("_");
    if ( ix >= 0 )
    {
      accel = name.charAt(ix + 1);
      sb.deleteCharAt(ix);
    }
    return sb.toString();
  }

  /** Adds a separator to the menu */
  public void addSeparator()
  {
    if ( ispopup )
    {
      popupMenu.addSeparator();
    }
    else
    {
      menu.addSeparator();
    }

  }

  /** Adds items for all actions available in the specified class */
  public void addAllItems(Class<?> model)
  {
    int lvl = -9999;
    for (TaskEntry te : TaskManager.getInstance().listAvailable(model))
    {
      MetaTask mt = te.getMeta();
      if ( mt == null )
      {
        lvl = -9999;
      }
      else
      {
        int n = mt.seqId();
        if ( n > (lvl + 1) && lvl > 0 )
        {
          addSeparator();
        }
        lvl = n;
      }
      addItem(te);
    }
  }

  /**
   * Adds an item for the given method name
   *
   * @param lbl eg "method"
   * @return TaskEntry
   * @throws TaskNotFoundException
   */
  public TaskEntry addItem(String lbl)
      throws TaskNotFoundException
  {
    String key = TaskEntry.getBaseKey(subject.getClass()) + lbl;
    TaskEntry te = tm.findEntry(key);
    return addItem(te);
  }

  /**
   * Adds a menu item for a given action key
   *
   * @param key eg "ClassName@method"
   * @return TaskEntry
   * @throws TaskNotFoundException
   */
  public TaskEntry addItemKey(String key)
      throws TaskNotFoundException
  {
    TaskEntry te = tm.findEntry(key);
    return addItem(te);
  }

  /** Adds a menu item for the provided task entry */
  public TaskEntry addItem(TaskEntry te)
  {
    MetaTask mt = te.getMeta();
    ImageIcon icon = GuiIconManager.getIcon(mt.icon());
    String text = getMnemonic(mt.label());
    JMenuItem mi;
    switch (type & 0x3)
    {
      case TEXT_AND_ICON :
        mi = new JMenuItem(text, icon);
        break;
      case ICON_ONLY :
        mi = new JMenuItem(icon);
        break;
      case TEXT_ONLY :
        mi = new JMenuItem(text);
        break;
      default :
        mi = new JMenuItem(text);
        break;
    }
    String key = te.getKey();
    mi.setActionCommand(key);
    mi.addActionListener(this);
    mi.setToolTipText(mt.hint());
    if ( accel > 0 )
    {
      mi.setMnemonic(accel);
    }
    map.put(key, mi);
    if ( ispopup )
    {
      popupMenu.add(mi);
    }
    else
    {
      menu.add(mi);
      TaskManager.getInstance().addPropertyChangeListener(key, this);
    }
    // tm.addPropertyChangeListener(key, this);
    return te;
  }

  /** Add a GuiMenu as a sub-menu */
  public void addSubMenu(GuiMenu subMenu)
  {
    if ( ispopup )
    {
      popupMenu.add(subMenu.getMenu());
    }
    else
    {
      menu.add(subMenu.getMenu());
    }
  }

  /** Adds a JMenu as a sub menu */
  public void addSubMenu(JMenu subMenu)
  {
    if ( ispopup )
    {
      popupMenu.add(subMenu);
    }
    else
    {
      menu.add(subMenu);
    }
  }

  /**
   * Implemented for PropertyChangeListener, handles enable/disable of menu items based on the task being enabled/disabled in task manager
   */
  public void propertyChange(PropertyChangeEvent evt)
  {
    String key = evt.getPropertyName();
    if ( key != null )
    {
      TaskEntry te = (TaskEntry) evt.getNewValue();
      JMenuItem mi = map.get(key);
      if ( mi != null )
      {
        mi.setEnabled(te.isEnabled());
      }
    }
  }

  /**
   * Implements ActionListener.actionPerformed to handle selected menu item's action
   * 
   * @param e
   */
  public void actionPerformed(ActionEvent e)
  {
    String key = e.getActionCommand();
    fireTask(key);
  }

  protected void fireTask(String key)
  {
    Task task;
    try
    {
      task = tm.getActionTask(key, subject, log);
      task.setDataObject(dataObject);
      tm.doTask(task, TASK_SPAWN);
    }
    catch (TaskNotFoundException e1)
    {
      e1.printStackTrace();
    }
    if ( subject instanceof GuiScreen )
    {
      ((GuiScreen) subject).undo();
    }
  }

  public void setSubject(Object subject)
  {
    if ( subject == null )
    {
      throw new NullPointerException("Subject may not be null");
    }
    this.subject = subject;
  }

  public void setLogger(ILogger logger)
  {
    if ( logger != null )
    {
      log = logger;
    }
    else
    {
      log = LogManager.getLogger(GuiMenu.class);
    }
  }

  /** Check for pop-up trigger, show pop-up if needed */
  private void maybeShowPopup(MouseEvent e)
  {
    if ( e.isPopupTrigger() && ispopup )
    {
      triggerComponent = e.getComponent();
      popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
  }

  // MouseListener - check pop-up
  public void mousePressed(MouseEvent e)
  {
    maybeShowPopup(e);
  }

  // MouseListener - check pop-up
  public void mouseReleased(MouseEvent e)
  {
    maybeShowPopup(e);
    maybeClick(e);
  }

  // check is an onClick key was given and action if needed
  private void maybeClick(MouseEvent e)
  {
    if ( ispopup && !e.isPopupTrigger() )
    {
      if ( onClickMethod != null )
      {
        triggerComponent = e.getComponent();
        String onClickKey = subject.getClass().getSimpleName() + "@" + onClickMethod;
        fireTask(onClickKey);
      }
    }
  }

  public JPopupMenu getPopupMenu()
  {
    return popupMenu;
  }

  public JMenu getMenu()
  {
    return menu;
  }

  public Component getTriggerComponent()
  {
    return triggerComponent;
  }

  public String getTriggerComponentName()
  {
    return triggerComponent == null ? null : triggerComponent.getName();
  }

  public Object getDataObject()
  {
    return dataObject;
  }

  public void setDataObject(Object dataObject)
  {
    this.dataObject = dataObject;
  }

  public Component[] getComponents()
  {
    if ( ispopup )
    {
      return popupMenu.getComponents();
    }
    else
    {
      return menu.getComponents();
    }
  }

  public String getOnClickMethod()
  {
    return onClickMethod;
  }

  public void setOnClickMethod(String onClickMethod)
  {
    this.onClickMethod = onClickMethod;
  }
}
