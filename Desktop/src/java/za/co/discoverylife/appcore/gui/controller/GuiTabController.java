package za.co.discoverylife.appcore.gui.controller;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import za.co.discoverylife.appcore.gui.GuiPanel;

/**
 * Implements main screen control as tabbed panes.
 * 
 * @author anton11
 */
public class GuiTabController
    extends GuiController
{
  private final GuiClosableTabbedPane pane;

  /**
   * CONSTRUCTOR creates the controller
   */
  public GuiTabController(boolean wrap)
  {
    int layout = wrap ? JTabbedPane.WRAP_TAB_LAYOUT : JTabbedPane.SCROLL_TAB_LAYOUT;
    pane = new GuiClosableTabbedPane(JTabbedPane.TOP, layout);
  }

  @Override
  public JComponent getComponent()
  {
    return pane;
  }

  @Override
  public GuiPanel selectPanel(String panelName)
  {
    GuiPanel p = findPanel(panelName);
    GuiPanel ap = setActivePanel(p);
    pane.setSelectedComponent(ap);
    return ap;
  }

  public void addPanel(GuiPanel panel, boolean allowClose)
  {
    super.addPanel(panel, allowClose);
    String title = panel.getName() + "  ";
    String tip = panel.getToolTipText();
    Icon icon = null;
    int n = pane.getTabCount();
    pane.insertTab(title, icon, panel, tip, n);
  }

  @Override
  public void removePanel(String panelName)
  {
    GuiPanel p = findPanel(panelName);
    super.removePanel(panelName);
    pane.remove(p);
  }

  @Override
  public GuiPanel getActivePanel()
  {
    GuiPanel p = findPanel(pane.getSelectedComponent().getName());
    setActivePanel(p);
    return super.getActivePanel();
  }

}
