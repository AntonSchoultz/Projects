package za.co.discoverylife.appcore.gui.controller;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JPanel;

import za.co.discoverylife.appcore.field.IEditable;
import za.co.discoverylife.appcore.gui.GuiPanel;
import za.co.discoverylife.appcore.gui.GuiScreen;
import za.co.discoverylife.appcore.gui.GuiScrollPanel;
import za.co.discoverylife.appcore.gui.screens.GuiBrowserScreen;
import za.co.discoverylife.appcore.gui.screens.GuiEditScreen;
import za.co.discoverylife.appcore.gui.screens.GuiViewScreen;
//import za.co.discoverylife.appcore.DebugObject;

/**
 * Manages the main screen
 * 
 * @author anton11
 */
public abstract class GuiController // extends DebugObject
    implements
      IEditable
{
  private HashMap<String, GuiPanel> map = new HashMap<String, GuiPanel>();

  private GuiPanel activePanel;

  /**
   * Add the provided panel, defaulting allow close to true.
   * 
   * @param panel
   */
  public void addPanel(GuiPanel panel)
  {
    addPanel(panel, true);
    //wr("Added '"+panel.getName()+"' true");
  }

  /**
   * Adds the provided panel to the model and makes it active (selects it). The
   * panel can be referred to by it's name (from JPanel.getName()) If a panel of
   * the same already exists, it will be replaced by the new one.
   * 
   * @param panel
   *          Panel to add
   * @param allowClose
   *          true=tab may be closed.
   * @return 
   */
  public synchronized void addPanel(GuiPanel panel, boolean allowClose)
  {
    //wr("Adding '"+panel.getName()+"' "+allowClose);
    String name = panel.getName();
    panel.setCloseable(allowClose);
    if ( name == null || name.trim().length() == 0 )
    {
      throw new InvalidParameterException("The provided panel must have it's name set - setName(~)");
    }
    GuiPanel x = map.get(name);
    if ( x != null )
    {
      //wr("Panel already exists with name. "+name);
    }
    map.put(name, panel);

  }

  /**
   * Removes the named panel from the model.
   * 
   * @param panelName
   */
  public synchronized void removePanel(String panelName)
  {
    GuiPanel old = map.get(panelName);
    if ( old != null )
    {
      //wr("Removing panel entry from map for '"+panelName+"'");
      map.remove(panelName);
    }
    else
    {
      //wr("Panel not found for remove '"+panelName+"'");
    }
  }

  /**
   * Removes all panels whose name ends with the provided text
   * 
   * @param ending
   */
  public void removeAllEndingWith(String ending)
  {
    ArrayList<String> ary = new ArrayList<String>();
    for (String name : map.keySet())
    {
      if ( name.endsWith(ending) )
      {
        ary.add(name);
      }
    }
    for (String name : ary)
    {
      removePanel(name);
    }
  }

  /**
   * Removes all panels whose name starts with the provided text
   * 
   * @param starting
   */
  public void removeAllStartingWith(String starting)
  {
    ArrayList<String> ary = new ArrayList<String>();
    for (String name : map.keySet())
    {
      if ( name.startsWith(starting) )
      {
        ary.add(name);
      }
    }
    for (String name : ary)
    {
      removePanel(name);
    }
  }

  /**
   * Returns the named panel, or null if not found
   * 
   * @param panelName
   * @return
   */
  public GuiPanel findPanel(String panelName)
  {
    return map.get(panelName);
  }

  /**
   * Returns the currently active panel
   * 
   * @return
   */
  public GuiPanel getActivePanel()
  {
    return activePanel;
  }

  public GuiPanel setActivePanel(GuiPanel panel)
  {
    activePanel = panel;
    if ( activePanel instanceof IEditable )
    {
      ((IEditable) activePanel).undo();
    }
    return activePanel;
  }

  public int getPanelCount()
  {
    return map.size();
  }

  /** IEditable.undo to all children */
  public synchronized void undo()
  {
    for (JPanel p : map.values())
    {
      if ( p instanceof IEditable )
      {
        try
        {
          ((IEditable) p).undo();
        }
        catch (Exception e)
        {
        }
      }
      else
      {
      }
    }
  }

  /** IEditable.comit to all children */
  public synchronized void commit()
  {
    if ( activePanel instanceof IEditable )
    {
      ((IEditable) activePanel).commit();
    }
  }

  /** IEditable.resett to all children */
  public synchronized void reset()
  {
    if ( activePanel instanceof IEditable )
    {
      ((IEditable) activePanel).reset();
    }
  }

  /**
   * Selects the named panel and makes it active. This panel is then available
   * via {@link #getActivePanel()}
   * 
   * @param panelName
   * @return
   */
  public abstract JPanel selectPanel(String panelName);

  /** Returns the wrapper component which will be the main screen */
  public abstract JComponent getComponent();

  /** Wraps the provided Panel in a ScrollPanel and adds to the main screen */
  public void addScrollablePanel(GuiPanel panel)
  {
    GuiScrollPanel scrollPanel = new GuiScrollPanel(panel);
    addPanel(scrollPanel);
    selectPanel(panel.getName());
  }

  /** Displays the specified Html file in it's own mini-web Browser tab */
  public void showInMiniBrowser(File fHtml)
  {
    String tab = fHtml.getName();
    showInMiniBrowser(tab, fHtml);
  }

  /** Displays the specified Html file in it's own mini-web Browser tab */
  public void showInMiniBrowser(String tab, File fHtml)
  {
    removePanel(tab);
    GuiBrowserScreen viewHtml = new GuiBrowserScreen(tab, fHtml);
    addPanel(viewHtml);
    selectPanel(tab);
  }

  /** Displays the specified file in it's own tab */
  public void showFile(File fText)
  {
    showEditFile(fText, false);
  }

  /** Displays the specified file in it's own tab */
  public void editFile(File fText)
  {
    showEditFile(fText, true);
  }

  /** Displays the specified file in it's own tab */
  public void showEditFile(File fText, boolean edit)
  {
    String tab = fText.getName();
    showEditFile(tab, fText, edit);
  }

  /** Displays the specified file in it's own tab */
  public void showEditFile(String tab, File fText, boolean edit)
  {
    removePanel(tab);
    GuiScreen scrn = null;
    if ( !edit )
    {
      scrn = new GuiViewScreen(tab, fText);
    }
    else
    {
      scrn = new GuiEditScreen(tab, fText, true, true);
    }
    tab = scrn.getName();
    removePanel(tab);
    if ( edit )
    {
      addPanel(scrn);
    }
    else
    {
      addScrollablePanel(scrn);
    }
    selectPanel(tab);
  }

  /** Displays the provided htmlText in it's own tab */
  public void showHtmlText(String tab, String html)
  {
    removePanel(tab);
    GuiScreen scrn = null;
    scrn = new GuiViewScreen(html);
    scrn.setName(tab);
    removePanel(tab);
    addScrollablePanel(scrn);
    selectPanel(tab);
  }

  /** Closes all panels */
  public void removeAllPanels()
  {
    ArrayList<String> list = new ArrayList<String>();
    list.addAll(map.keySet());
    for (String s : list)
    {
      removePanel(s);
    }
  }

}
