package za.co.discoverylife.appcore.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import za.co.discoverylife.appcore.ApplicationBase;
import za.co.discoverylife.appcore.DataHolder;
import za.co.discoverylife.appcore.gui.buttons.GuiButton;
import za.co.discoverylife.appcore.gui.controller.GuiController;
import za.co.discoverylife.appcore.gui.screens.GuiLogger;
import za.co.discoverylife.appcore.gui.screens.GuiStatus;
import za.co.discoverylife.appcore.gui.screens.GuiViewScreen;
import za.co.discoverylife.appcore.logging.LogManager;
import za.co.discoverylife.appcore.logging.Logger;
import za.co.discoverylife.appcore.plugin.IPlugInConstants;
import za.co.discoverylife.appcore.plugin.ModuleEntry;
import za.co.discoverylife.appcore.task.ILinkAction;
import za.co.discoverylife.appcore.task.LinkTask;
import za.co.discoverylife.appcore.task.MetaMenu;
import za.co.discoverylife.appcore.task.MetaTask;
import za.co.discoverylife.appcore.task.TaskManager;
//import za.co.discoverylife.bob.menues.HelpMenu;

/**
 * Provides the basis for a GUI Application
 * 
 * @author anton11
 */
@MetaMenu(label = "_File")
public abstract class GuiBase extends ApplicationBase implements Runnable, ActionListener // , WindowListener
{
  private static final long serialVersionUID = 4554027857571978297L;
  protected static int RC = 0;
  protected static GuiBase guiBase;

  /** persisted attributes */
  protected int top = 150;
  protected int left = 200;
  protected int width = 0;// 700;
  protected int height = 450;
  protected int FontSize = 11;
  // Button styles 0=Default, 1=Icon, 2=Text, 3=Both
  protected int buttonStyle = GuiButton.BUTTON_STYLE_DEFAULT;
  // log level for GuiConsole Logger
  protected int guiLogLevel = Logger.LOG_ALL;
  protected int guiLogRows = 5;
  protected File currentWorkFile;

  protected transient JPanel logPanel = new JPanel();
  protected transient GuiStatus guiStatus;
  protected transient JSplitPane splitter;
  protected transient JMenu toolsMenu = null;
  private transient Timer autoUpdateTimer;
  protected transient JFrame frame;
  protected transient GuiController guiController;
  protected transient String appIconName = null;
  protected transient JMenuBar menuBar = null;
  protected transient GuiLogger guiLogger;
  protected transient boolean DIR = true;
  protected transient boolean FILE = false;
  protected transient String heading = "";

  /**
   * CONSTRUCT application and restore state
   */
  public GuiBase()
  {
    super();
    // default Font size is 10 points
    if ( FontSize <= 0 )
    {
      FontSize = 11;
    }
    GuiLookAndFeel.useFontSize(FontSize);
  }

  /**
   * Called from the app's main() method to launch the GUI in it's own thread
   * 
   * @param guiapp
   */
  protected static void launchGUIApp(GuiBase guiapp)
  {
    guiBase = guiapp;
    guiBase.log.info("Starting GUI for " + guiBase.appName);
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        guiBase.createAndShowGUI();
      }
    });
  }

  /** Sets the font size to use */
  public void setFontSize(int fontSize)
  {
    FontSize = fontSize;
    GuiLookAndFeel.useFontSize(FontSize);
    getGuiController().removeAllPanels();
    reshow();
  }

  /** Returns version information (from manifest) */
  public String getVersion()
  {
    String v = getVersion(this);
    return v == null ? "unkown" : v;
  }

  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event-dispatching thread.
   */
  private void createAndShowGUI()
  {
    initModels();// data and actions
    // do screen
    try
    {
      UIManager.setLookAndFeel(new GuiLookAndFeel());
    }
    catch (UnsupportedLookAndFeelException e)
    {
      e.printStackTrace();
    }
    frame = new GuiFrame();
    //frame.addWindowListener(this);
    //frame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    //frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    heading = appName;
    setTitle(null);
    setScreenSizeAndPosition();
    ImageIcon icon = GuiIconManager.getIcon(appIconName == null ? appName : appIconName);
    if ( icon != null )
    {
      frame.setIconImage(icon.getImage());
    }
    else
    {
      System.out.println("Could not find icon for '" + appName + "'");
    }
    GuiButton.setButtonStyle(buttonStyle);
    guiController = initGuiController();// main screen layout, split panes etc
    initMenus();// main menu bar and sub-menus(from models)

    JComponent mainComponent = guiController.getComponent();
    InputMap inmap = mainComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    inmap.put(KeyStroke.getKeyStroke("F12"), "help");
    mainComponent.getActionMap().put("help", new GuiHelpAction());

    splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainComponent, logPanel);
    splitter.setPreferredSize(new Dimension(width, height));
    splitter.setOneTouchExpandable(true);
    splitter.setDividerLocation(height);
    frame.getContentPane().add(splitter);

    guiStatus = new GuiStatus();
    frame.add(guiStatus, BorderLayout.SOUTH);
    postInit();

    // adds F12 -> help HTML page
    File fIcon = new File(appName + ".PNG");
    try
    {
      if ( !fIcon.exists() )
      {
        GuiIconManager.saveIcon(icon, fIcon);
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    frame.pack();
    frame.setVisible(true);
    splitter.setDividerLocation(0.80d);
  }

  /** Display the text in a pop-up dialog box */
  public void displayTextResourceAsDialogue(String textFileSpec, String title)
  {
    try
    {
      // String ver = getVersion();
      GuiViewScreen viewNotes = new GuiViewScreen(title, textFileSpec);
      showAsDialog(viewNotes);
    }
    catch (IOException e)
    {
      log.error("Error displaying resource " + textFileSpec, e);
    }
  }

  /** Display the text in a pop-up dialog box */
  public void displayTextResourceAsPanel(String textFileSpec, String title)
  {
    try
    {
      GuiViewScreen viewNotes = new GuiViewScreen(title, textFileSpec);
      guiController.removePanel(viewNotes.getName());
      guiController.addScrollablePanel(viewNotes);
      guiController.selectPanel(viewNotes.getName());
    }
    catch (IOException e)
    {
      log.error("Error displaying resource " + textFileSpec, e);
    }
  }

  /** Return sub - Dimension of screen to given percentages */
  public Dimension getScreenSize(int pctWidth, int pctHeight)
  {
    return new Dimension(width * pctWidth / 100, height * pctHeight / 100);
  }

  /** Return sub - Dimension of screen */
  public Dimension getScreenSize()
  {
    return new Dimension(width, height);
  }

  /** Displays the provided Panel as a modal dialogue */
  public JDialog showAsDialog(GuiPanel panel)
  {
    return showAsDialog(panel, getScreenSize(80, 90));
  }

  /** Displays the provided Panel as a modal dialogue */
  public JDialog showAsDialog(File fHtml)
  {
    GuiViewScreen scrn = new GuiViewScreen(fHtml.getName(), fHtml);
    return showAsDialog(scrn, getScreenSize(80, 90));
  }

  /** Displays the provided Panel as a scrollable modal dialogue */
  public JDialog showAsDialog(GuiPanel panel, Dimension dim)
  {
    JDialog jd = new JDialog(frame, panel.getName(), true);
    jd.setPreferredSize(dim);
    Container contentPane = jd.getContentPane();
    GuiScrollPanel scrollPanel = new GuiScrollPanel(panel);
    contentPane.add(scrollPanel, BorderLayout.CENTER);
    jd.pack();
    jd.setLocationRelativeTo(frame);
    jd.setModal(true);
    jd.setVisible(true);
    return jd;
  }

  /** Display the provided GuiScreen as a modal Dialogue */
  public String showAsGuiDialog(GuiScreen screen, Dimension dim)
  {
    GuiPanel.adjustSize(screen, dim);
    return GuiDialog.showDialog(frame, screen);
  }

  /** Display the provided GuiScreen as a modal Dialogue */
  public String showAsGuiDialog(GuiScreen screen)
  {
    Dimension dim = getScreenSize(80, 90);
    return showAsGuiDialog(screen, dim);
  }

  /**
   * Display the prompt text provided, and collect input value from user.
   * 
   * @param oldValue
   *          Current value (default used if user leaves field blank)
   * @param promptText
   *          Text to be displayed to request information.
   * @return user input (or old value as default)
   */
  public String promptString(String oldValue, String promptText)
  {
    String value = (String) JOptionPane.showInputDialog(frame, promptText, "Enter String",
        JOptionPane.QUESTION_MESSAGE, (Icon) null, null, oldValue);
    return value.trim();
  }

  /**
   * Display a selection of options and allow user to choose one.
   * 
   * @param optns
   *          Array of strings, each representing an option
   * @param oldValue
   *          current value
   * @return selected value
   */
  public String promptSelect(String[] optns, String oldValue, String promptText)
  {
    return (String) JOptionPane.showInputDialog(frame, promptText, "Select item", JOptionPane.QUESTION_MESSAGE,
        (Icon) null, optns, oldValue);
  }

  /**
   * Display a prompt for confirmation.
   * @param promptText Text to display prompt for
   * @return true if user selects [OK]
   */
  public boolean promptConfirm(String promptText)
  {
    int n = JOptionPane.showConfirmDialog(frame, promptText, "Confirm", JOptionPane.YES_NO_OPTION);
    return (n == JOptionPane.OK_OPTION);
  }

  /** Called after initialisation of the screen */
  public abstract void postInit();

  /** Sets title of the GUI window */
  public void setTitle(String title)
  {
    frame.setTitle(title == null ? heading : heading + " : " + title);
  }

  /**
   * Initialise models (data and actions)
   * Called before creating the screen
   */
  public abstract void initModels();

  /**
   * Initialise main menu bar and sub-menus(from models)
   * Called during creation of the screen
   */
  public abstract void initMenus();

  /**
   * Called to create, initialise and return the main screen controller
   */
  public abstract GuiController initGuiController();

  /**
   * Redisplay the GUI ( invalidate() the innermost/deepest item to update
   * first)
   */
  public void reshow()
  {
    if ( frame != null && frame.getFocusOwner() != null )
    {
      frame.invalidate();
      for (Component comp : frame.getComponents())
      {
        comp.invalidate();
        comp.repaint();
      }
      frame.pack();
      frame.setVisible(true);
      frame.repaint();
    }
  }

  /** Update display to reflect data values */
  public static void undo()
  {
    guiBase.guiController.undo();
  }

  /** Start Automatic update of the screen every mSec */
  public void startAutoUpdate(int mSec)
  {
    autoUpdateTimer = new Timer(mSec, (ActionListener) this);
    autoUpdateTimer.start();
  }

  /** Start Automatic update of the screen every mSec */
  public void stopAutoUpdate()
  {
    autoUpdateTimer.stop();
  }

  /** responds to timer actions by repainting the screen */
  public void actionPerformed(ActionEvent evt)
  {
    if ( !shutdown )
    {
      if ( frame.getFocusOwner() != null )
      {
        readScreen();
        writeScreen();
        reshow();
      }
    }
  }

  /**
   * Set screen size and position from the class parameters top,left,width and
   * height.
   */
  public void setScreenSizeAndPosition()
  {
    writeScreen();
    GuiButton.setButtonStyle(buttonStyle);
  }

  /**
   * Locate and size the screen.
   */
  public void writeScreen()
  {
    if ( width <= 0 )
    {
      Dimension scrnDim = Toolkit.getDefaultToolkit().getScreenSize();
      // System.out.println("Screen size = "+scrnDim.width+" x "+scrnDim.height);
      width = scrnDim.width * 3 / 4;
      height = scrnDim.height * 3 / 4;
      if ( (width * 100 / height) > 150 )
      {
        width = width / 2;
      }
      top = height / 12;
      left = width / 12;
    }
    // System.out.println("Frame:"+width+" x "+height+" @ "+left+","+top);
    Dimension dim = new Dimension(width, height);
    Point topLeft = new Point(left, top);
    frame.setPreferredSize(dim);
    frame.setMaximumSize(dim);
    frame.setMinimumSize(new Dimension(200, 150));
    frame.setLocation(topLeft);
  }

  /**
   * Reads current screen size and position, and sets the values in the class
   * members accordingly.
   */
  public void getScreenSizeAndPosition()
  {
    try
    {
      log.debug("GetScreenSizeAndPosition");
      readScreen();
      log.debug("Get button style");
      buttonStyle = GuiButton.getButtonStyle();
      log.debug("Get gui log config (level and rows)");
      if ( guiLogger != null )
      {
        guiLogLevel = guiLogger.getDisplayLogLevel();
        guiLogRows = guiLogger.getDisplayLogRows();
      }
      log.debug("Done");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public void readScreen()
  {
    Dimension dim = frame.getSize();
    Point da = frame.getLocation();
    width = dim.width;
    height = dim.height;
    top = da.y;
    left = da.x;
  }

  /** Return GUI */
  public static GuiBase getGuiBase()
  {
    return guiBase;
  }

  /** Adds a Console Logger window to the screen */
  protected void createGuiLogger()
  {
    if ( guiLogger == null )
    {
      guiLogger = new GuiLogger();
      guiLogger.setDisplayLogLevel(guiLogLevel);
      guiLogger.setDisplayLogRows(guiLogRows);
      taskManager.registerModel(GuiLogger.class);
      DataHolder.store(guiLogger);
      guiLogger.update(guiLogLevel);
    }
  }

  /** Adds a Console Logger window to the screen */
  protected void addGuiLogger()
  {
    createGuiLogger();
    logPanel.add(guiLogger);
    splitter.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, guiLogger);
    splitter.setDividerLocation(0.80);
  }

  /** Remove the GUI Logger (part of preparing to shutdown) */
  protected void removeGuiLogger()
  {
    if ( guiLogger != null )
    {
      // save the log level first
      getGuiLogLevel();
      getGuiLogRows();
      LogManager.getInstance().removeLogListener(guiLogger);
      // frame.remove(guiLogger);
      logPanel.remove(guiLogger);
      splitter.removePropertyChangeListener(guiLogger);
      guiLogger = null;
    }
  }

  /** Create a menu bar (if not already done) and return it */
  public JMenuBar createMenuBar()
  {
    if ( menuBar == null )
    {
      menuBar = new JMenuBar();
      frame.setJMenuBar(menuBar);
      frame.pack();
      frame.setVisible(true);
    }
    return menuBar;
  }

  /** Add a separator to the menu bar */
  protected void createMenuSeparator()
  {
    createMenuBar();
    JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
    Dimension dim = menuBar.getMaximumSize();
    sep.setMaximumSize(new Dimension(10, dim.height));
    menuBar.add(sep);
  }

  /** add padding in menu bar (preceding end of line menues) */
  protected void createMenuPadding()
  {
    createMenuBar();
    menuBar.add(Box.createHorizontalGlue());
  }

  /** Create a menu based on the @Meta tags in the supplied object */
  public GuiMenu createMenu(Object model)
  {
    MetaMenu mm = model.getClass().getAnnotation(MetaMenu.class);
    String name = model.getClass().getSimpleName();
    String hint = model.getClass().getName() + " does not have a hint!";
    if ( mm != null )
    {
      name = mm.label();
      hint = mm.hint();
    }
    return createMenu(name, model, hint);
  }

  /**
   * Create a menu based on the @Meta tags in the supplied object, using the
   * provided name and tool tip
   */
  public GuiMenu createMenu(String name, Object model, String hint)
  {
    createMenuBar();
    TaskManager.getInstance().registerModel(model.getClass());
    GuiMenu mnu = new GuiMenu(model, log, name, GuiMenu.TEXT_ONLY, false, hint);
    mnu.addAllItems(model.getClass());
    menuBar.add(mnu.getMenu());
    return mnu;
  }

  /**
   * Creates and adds plug-in main menu items
   */
  public void createPlugInMenues()
  {
    for (String modelClassName : moduleList.getClassNames(IPlugInConstants.PLUGIN_TOOLS_MENU))
    {
      try
      {
        Class<?> modelClass = Class.forName(modelClassName);
        Object model = modelClass.newInstance();
        MetaMenu mm = model.getClass().getAnnotation(MetaMenu.class);
        String name = model.getClass().getSimpleName();
        String hint = model.getClass().getName() + " does not have a hint!";
        if ( mm != null )
        {
          name = mm.label();
          hint = mm.hint();
        }
        TaskManager.getInstance().registerModel(model.getClass());
        GuiMenu mnu = new GuiMenu(model, log, name, GuiMenu.TEXT_ONLY, false, hint);
        mnu.addAllItems(model.getClass());
        if ( toolsMenu == null )
        {
          toolsMenu = new JMenu("Tools");
          toolsMenu.setToolTipText("Plug in tools");
          toolsMenu.setMnemonic('T');
          menuBar.add(toolsMenu);
        }
        toolsMenu.add(mnu.menu);
        System.out.println("PlugIn:Added menu " + mnu.getMenu().getName());
      }
      catch (Exception e)
      {
        System.err.println("Error adding TOOLS_MENU :" + modelClassName);
        e.printStackTrace();
      }
    }
  }

  @SuppressWarnings("unchecked")
  /**
   * Register Link actions from plug-in modules
   */
  public void addPlugInLinkActions()
  {
    for (String classname : moduleList.getClassNames(IPlugInConstants.PLUGIN_LINK_ACTION))
    {
      try
      {
        Class<? extends ILinkAction> linkClass = (Class<? extends ILinkAction>) Class.forName(classname);
        LinkTask.registerLinkAction(linkClass);
      }
      catch (ClassNotFoundException e)
      {
        log.error("Problem linking action for " + classname, e);
      }
    }
  }

//  /** Adds a sub-menu for plugin help where required */
//  protected void addPluginHelpSubMenu(HelpMenu helpMenu, GuiMenu helpDropMenu)
//  {
//    GuiMostRecentlyUsed phl = new GuiMostRecentlyUsed("Tools");
//    phl.setOpener(helpMenu);
//    for (ModuleEntry me : moduleList.getModules())
//    {
//      String name = me.getHelpName();
//      if ( name != null && name.length() > 0 )
//      {
//        phl.addItem(name);
//      }
//    }
//    if ( phl.getSize() > 0 )
//    {
//      helpDropMenu.addSeparator();
//      helpDropMenu.addSubMenu(phl.getMenu());
//    }
//  }

  /** Sets display message */
  public void setStatus(String message)
  {
    guiStatus.setStatus(message);
  }

  /** returns status line object */
  public GuiStatus getStatusLine()
  {
    return guiStatus;
  }

  /**
   * Returns the current logging level for the GUI Logger (shutdown persistance)
   */
  public int getGuiLogLevel()
  {
    if ( guiLogger != null )
    {
      guiLogLevel = guiLogger.getDisplayLogLevel();
    }
    return guiLogLevel;
  }

  /**
   * Sets the current logging level for the GUI Logger (startup initialisation )
   */
  public void setGuiLogLevel(int guiLogLevel)
  {
    this.guiLogLevel = guiLogLevel;
    if ( guiLogger != null )
    {
      guiLogger.setDisplayLogLevel(guiLogLevel);
    }
  }

  /**
   * Returns the current logging level for the GUI Logger (shutdown persistance)
   */
  public int getGuiLogRows()
  {
    if ( guiLogger != null )
    {
      guiLogRows = guiLogger.getDisplayLogRows();
    }
    return guiLogRows;
  }

  /**
   * Sets the current logging level for the GUI Logger (startup initialisation )
   */
  public void setGuiLogRows(int guiLogRows)
  {
    this.guiLogRows = guiLogRows;
    if ( guiLogger != null )
    {
      guiLogger.setDisplayLogRows(guiLogRows);
    }
  }

  @Override
  protected void shutdown()
  {
    stopAutoUpdate();
    removeGuiLogger();
    super.shutdown();
  }

  /**
   * Pop up a file/directory selection dialog. Used for open file / save as etc
   * ...
   * 
   * @param fin
   * @param title
   * @param isdir
   * @return
   */
  public File openFile(File fin, String title, boolean isdir)
  {
    return pickFile(fin, title, isdir, false);
  }

  /**
   * Pop up a file/directory selection dialog. Used for open file / save as etc
   * ...
   * 
   * @param fin
   * @param title
   * @param isdir
   * @return
   */
  public File saveFile(File fin, String title, boolean isdir)
  {
    return pickFile(fin, title, isdir, true);
  }

  /**
   * Pop up a file/directory selection dialog. Used for open file / save as etc
   * ...
   * 
   * @param fin
   * @param title
   * @param isdir
   * @return
   */
  public File pickFile(File fin, String title, boolean isdir, boolean isSave)
  {
    JFileChooser fc = new JFileChooser();
    File fOut = fin;
    String loadSave = isSave ? "Save:" : "Load:";
    try
    {
      fc.setDialogTitle(loadSave + ":" + title + (isdir ? " folder" : " file"));
      fc.setFileSelectionMode(isdir ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY);
      if ( fin == null )
      {
        fin = workDir;
      }
      File fDir = fin;
      if ( !fDir.isDirectory() )
      {
        fDir = fin.getParentFile();
      }
      fc.setCurrentDirectory(fDir);
      if ( fin.isFile() )
      {
        fc.setSelectedFile(fin);
      }
      int rc = JFileChooser.CANCEL_OPTION;
      if ( isSave )
      {
        rc = fc.showSaveDialog(frame);
      }
      else
      {
        rc = fc.showOpenDialog(frame);
      }
      if ( rc == JFileChooser.APPROVE_OPTION )
      {
        fOut = fc.getSelectedFile();
        if ( isdir && fOut.isFile() )
        {
          fOut = fOut.getParentFile();
        }
      }
      else
      {
        fOut = null;
      }
    }
    catch (Exception e)
    {
      System.err.println("openFile error");
      e.printStackTrace();
    }
    return fOut;
  }

  @MetaTask(seqId = 900, label = "E_xit", hint = "Exit the program")
  public void exit()
  {
    removeGuiLogger();
    System.exit(RC);
  }

  /** Returns text from clip-board (cut-paste) */
  public String getTextFromClipBoard()
  {
    String cliptext = null;
    try
    {
      Clipboard clipBoard = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
      cliptext = (String) clipBoard.getData(DataFlavor.stringFlavor);
    }
    catch (Exception e)
    {
      // ignore any errors from trying to access clip-board.
    }
    return cliptext;
  }

  public File getCurrentWorkFile()
  {
    return currentWorkFile;
  }

  public void setCurrentWorkFile(File currentWorkFile)
  {
    this.currentWorkFile = currentWorkFile;
  }

  public GuiController getGuiController()
  {
    return guiController;
  }

  public JMenuBar getMenuBar()
  {
    return menuBar;
  }

  public static int getRC()
  {
    return RC;
  }

  public static void setRC(int rC)
  {
    RC = rC;
  }

  public GuiLogger getGuiLogger()
  {
    return guiLogger;
  }

  public int getWidth()
  {
    return width;
  }

  public JFrame getFrame()
  {
    return frame;
  }

  public String getHeading()
  {
    return heading;
  }

  public void setHeading(String heading)
  {
    this.heading = heading;
  }

  //	// ========================================================================== WindowListeners
  //	public void windowOpened(WindowEvent e) {
  //	}
  //
  //	public void windowClosing(WindowEvent e) {
  //		System.err.println("**************************************");
  //		System.err.println("******   Closing                ******");
  //		System.err.println("**************************************");
  //	}
  //
  //	public void windowClosed(WindowEvent e) {
  //		System.err.println("**************************************");
  //		System.err.println("******   Closed                 ******");
  //		System.err.println("**************************************");
  //		//shutdown();
  //		exit();
  //	}
  //
  //	public void windowIconified(WindowEvent e) {
  //	}
  //
  //	public void windowDeiconified(WindowEvent e) {
  //	}
  //
  //	public void windowActivated(WindowEvent e) {
  //	}
  //
  //	public void windowDeactivated(WindowEvent e) {
  //	}

}
