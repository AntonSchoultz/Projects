package za.co.discoverylife.appcore.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Track most recently used files/folders
 * Updating a menu with the items.
 * 
 * @author anton11
 */
public class GuiMostRecentlyUsed implements ActionListener
{
  private List<String> mruFiles = new ArrayList<String>();
  private String title = "";
  private int maxEntries = 5;

  private transient JMenu menu = null;
  private transient IMostRecentOpener opener;

  /** CONSTRUCTOR */
  public GuiMostRecentlyUsed()
  {
    maxEntries = 5;
  }

  /** CONSTRUCTOR which accepts a title for the file dialogues */
  public GuiMostRecentlyUsed(String title)
  {
    this.title = title;

  }

  /** Register the class which actions the OPEN */
  public void setOpener(IMostRecentOpener opener)
  {
    this.opener = opener;
    menu = new JMenu(title);
    menu.setToolTipText("List of most recently used " + title);
  }

  public void addItem(String fileSpec)
  {
    mruFiles.add(fileSpec);
    refresh();
  }

  public int getSize()
  {
    return mruFiles.size();
  }

  /** Return the menu with the file options in it */
  public JMenu getMenu()
  {
    return menu;
  }

  public void refresh()
  {
    run();
  }

  /** updates the menu options to reflect the most recent entries */
  public void run()
  {
    menu.setIgnoreRepaint(true);
    menu.removeAll();
    List<String> sorted = new ArrayList<String>();
    sorted.addAll(mruFiles);
    Collections.sort(sorted);
    for (String s : sorted)
    {
      JMenuItem item = new JMenuItem(s);
      item.setActionCommand(s);
      item.addActionListener(this);
      menu.add(item);
    }
    menu.setIgnoreRepaint(false);
  }

  /** Updates the MRU list */
  public void updateMRUFile(String filename)
  {
    mruFiles.remove(filename);
    mruFiles.add(0, filename);
    while (mruFiles.size() > maxEntries)
    {
      mruFiles.remove(maxEntries);
    }
    refresh();
  }

  /** Opens the most recent file */
  public void openMostRecent()
  {
    if ( mruFiles.size() > 0 )
    {
      String filename = mruFiles.get(0);// top item is most recent
      opener.openRecent(title, filename);
    }
  }

  /** Responds to menu selection of a file from the most recent list */
  public void actionPerformed(ActionEvent e)
  {
    String filename = e.getActionCommand();
    opener.openRecent(title, filename);
  }

  public int getMaxEnties()
  {
    return maxEntries;
  }

  public void setMaxEnties(int maxEnties)
  {
    this.maxEntries = maxEnties;
  }

}
