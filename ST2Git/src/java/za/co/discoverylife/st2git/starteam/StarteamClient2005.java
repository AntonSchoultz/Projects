package za.co.discoverylife.st2git.starteam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;
import za.co.discoverylife.appcore.util.Validate;
import za.co.discoverylife.st2git.Reference;
import za.co.discoverylife.st2git.host.ICredentials;
import za.co.discoverylife.st2git.host.IServer;
import za.co.discoverylife.st2git.host.ServerSpecification;
import com.starbase.starteam.CheckoutEvent;
import com.starbase.starteam.CheckoutListener;
import com.starbase.starteam.CheckoutManager;
import com.starbase.starteam.CheckoutOptions;
import com.starbase.starteam.CheckoutProgress;
import com.starbase.starteam.File;
import com.starbase.starteam.Folder;
import com.starbase.starteam.Item;
import com.starbase.starteam.ItemList;
import com.starbase.starteam.Label;
import com.starbase.starteam.Project;
import com.starbase.starteam.Server;
import com.starbase.starteam.Status;
import com.starbase.starteam.Type;
import com.starbase.starteam.User;
import com.starbase.starteam.View;
import com.starbase.starteam.ViewConfiguration;

/**
 * Implements {@link IClientAdaptor} for StarTeam 2005 server.
 * 
 * @author Anton Schoultz (2013)
 */
public class StarteamClient2005 implements CheckoutListener
{
  /** Holds user and password */
  protected ICredentials credentials;
  protected IServer host;
  Logger log = Logger.getLogger("ST2Git");
  /** must check-out/in be forced ? */
  protected boolean force = false;
  protected boolean zapOrphans = false;;
  /** Folder where this project (root) lives on local machine */
  protected String localRoot;
  /** Source control specification for root of project in source control */
  protected String sourceRoot;
  /** Estimated file count */
  protected int fileCount;
  /** Number of files processed */
  protected int processed = 0;
  /** Holds the result of the check out - for history report etc */
  protected Result reportInformation;
  //
  private static final int RETRIES = 3;
  /** the StarTeam server that we are connecting to */
  Server stServer;
  /** StarTeam User */
  User stUser;
  /** StarTeam Project selected */
  Project stProject;
  /** Default view into the StarTeam Project */
  View stDefaultView;
  /** Selected (active) view being used to access the StarTeam Project */
  View stActiveView;
  /** The source label being used */
  Label stLabel;
  /** The root folder in the StarTeam Project */
  Folder stRoot;
  /** StarTeam folder being processed */
  private Folder stFolder;
  /** StarTEam file being processed */
  private com.starbase.starteam.File stFile;
  /** Starteam user id */
  private int stUserId;
  /** StarTEam label id (for labeled view) */
  private int stLabelID;
  /** Constant used by starteam to identify file objects */
  private String FILE;
  /** Constant used by starteam to identify file objects */
  protected static Type FILE_TYPE;
  /** Checkout option - end of line conversion */
  protected boolean eol = false;
  /** Checkout option - use of MD5 to establish file status */
  protected boolean useMD5 = true;
  /** Checkout option - should Starteam status be updated */
  protected boolean updateStatus = true;
  /** StarTeam Project Name */
  private String stProjectName;
  /** StarTeam checkout options eg overwrite, end-of-line etc */
  private CheckoutOptions stOptions;
  /** StarTeam checkout manager that handles a check out */
  private CheckoutManager coMan;
  /** Cached list of valid labels within starteam */
  private Label[] labels;
  /** Constant used by starteam to identify folder objects */
  private String FOLDER;
  /** Folder where this project (root) lives on local machine */
  private java.io.File fLocalRoot;
  /** File object on the local disk for the current check-out file */
  private java.io.File fTgt;
  /** Change report - Old Label's ID */
  private int stLabelIdOld;
  /** Change report - New Label's ID */
  private int stLabelIdNew;
  /** Flag to control how check-out occurs */
  private static boolean useWalker = true;
  Project[] projects;
  View[] views;

  /**
   * CONSTRUCTOR
   */
  public StarteamClient2005()
  {
    super();
  }

  public StarteamClient2005(ServerSpecification serverSpec) throws Exception
  {
    super();
    setServer(serverSpec);
    setCredentials(serverSpec);
    connect();
  }

  /**
   * Sets the address and port of the server.
   * 
   * @param host
   *          IServer - holds host address and port
   * @throws Exception
   *           for any errors
   */
  public void setServer(IServer host) throws Exception
  {
    this.host = host;
  }

  /**
   * Sets the logOn credentials to be used to access the server.
   * 
   * @param credentials
   *          ICredentials - holds logInUser and Password
   * @throws Exception
   *           for any errors
   */
  public void setCredentials(ICredentials credentials) throws Exception
  {
    this.credentials = credentials;
  }

  /**
   * Establish a connection to the server (logOn)
   * 
   * @throws Exception
   *           for any errors
   */
  public void connect() throws Exception
  {
    Validate.notNull(host, "Starteam server host not specified");
    stServer = new Server(host.getUrl(), host.getPort());
    stServer.connect();
    Validate.notNull(stServer, "Failed to connection to starteam server.");
    Validate.notNull(credentials, "Starteam credentials not given.");
    String user = credentials.getUser();
    String pass = credentials.getPassword();
    stUserId = stServer.logOn(user, pass);
    stUser = stServer.getUser(stUserId);
    Validate.notNull(stUser, "Failed to log on.");
    log.info("Logged on to server " + host.getUrl() + ":" + host.getPort() + " with user "
        + credentials.getUser());
  }

  /**
   * Disconnect (logOff) from server.
   * 
   * @throws Exception
   *           for any errors
   */
  public void disconnect() throws Exception
  {
    if (stServer != null)
    {
      stServer.disconnect();
      // $log.info("Logged off from server " + host.getUrl());
    }
  }

  /**
   * Cancel any current operations and disconnect.
   */
  public void cancel()
  {
    synchronized (this)
    {
      if (coMan != null)
      {
        coMan.setCanceled();
      }
    }
  }

  /**
   * Returns a list of all project names available
   * 
   * @return
   */
  public TreeSet<String> $listAllProjects()
  {
    TreeSet<String> list = new TreeSet<String>();
    projects = stServer.getProjects();
    for (Project p : projects)
    {
      list.add(p.getName());
    }
    return list;
  }

  /** Selects the named project */
  public void $selectProject(String projectName) throws Exception
  {
    stProject = null;
    stFolder = null;
    stProjectName = projectName;
    // --- find project
    projects = stServer.getProjects();
    for (int i = 0; i < projects.length; i++)
    {
      if (projects[i].getName().equalsIgnoreCase(stProjectName))
      {
        stProject = projects[i];
        break;
      }
    }
    if (stProject == null)
    {
      throw new Exception("Project '" + stProjectName + "' not found on server");
    }
    FILE = stProject.getTypeNames().FILE;
    FOLDER = stProject.getTypeNames().FOLDER;
    stDefaultView = stProject.getDefaultView();
    labels = stDefaultView.getLabels();
    if (localRoot != null)
    {
      stDefaultView.setAlternatePath(localRoot);
    }
    stActiveView = stDefaultView;
    stLabel = null;
    stLabelID = 0;
    FILE_TYPE = (new File(stActiveView.getRootFolder())).getType();
    // $log.info(stProjectName + " selected " + stActiveView.getRootFolder().getFolderHierarchy());
    stRoot = null;// flag that root must be recalculated
  }

  /** Return list of views by date/time order */
  public ArrayList<String> $listViews() throws Exception
  {
    ArrayList<String> list = new ArrayList<String>();
    views = stProject.getViews();
    for (View vu : views)
    {
      list.add(vu.getFullName());
    }
    Collections.sort(list);
    return list;
  }

  /** Select the named view */
  public void $selectView(String viewName)
  {
    stDefaultView = stProject.getDefaultView();
    stActiveView = stDefaultView;
    views = stProject.getViews();
    for (View vu : views)
    {
      if (vu.getFullName().compareTo(viewName) == 0)
      {
        stDefaultView = vu;
        stActiveView = vu;
        System.out.println("\t\tSelected view " + vu.getFullName());
        return;
      }
    }
    System.out.println("\t\tCould not find view " + viewName);
  }

  /** Return list of labels by date/time order */
  public ArrayList<Reference> $listLabels() throws Exception
  {
    ArrayList<Reference> list = new ArrayList<Reference>();
    labels = stDefaultView.getLabels();
    for (Label lbl : labels)
    {
      if (lbl.isDeleted())
      {
        continue;
      }
      Reference ref = new Reference(stProjectName, stActiveView.getFullName(), lbl);
      list.add(ref);
    }
    return list;
  }

  /** Select a labeled version within the current view */
  public void $selectLabel(String lableName) throws Exception
  {
    // selectLabeledView(lableName);
    labels = stDefaultView.getLabels();
    stLabel = null;
    for (Label label : labels)
    {
      if (label.getName().equalsIgnoreCase(lableName))
      {
        stLabel = label;
        break;
      }
    }
    if (stLabel == null)
    {
      throw new Exception("Label '" + lableName + "' not found.");
    }
    // ---
    try
    {
      stLabelID = stLabel.getID();
      // configure a filter for the required label
      ViewConfiguration viewConfig = ViewConfiguration.createFromLabel(stLabelID);
      stActiveView = new View(stDefaultView, viewConfig);
      log.info(stProjectName + "> Selected view for label " + lableName);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new Exception("Could not select view label '" + lableName + "' for "
          + stProject.getName()
          + ":" + e.getMessage());
    }
    stRoot = null;// flag that root must be recalculated
  }

  /** Select project, view and label ready for checkout */
  public void $select(String projectName, String viewName, String labelName) throws Exception
  {
    $selectProject(projectName);
    $selectView(viewName);
    $selectLabel(labelName);
  }

  /**
   * Sets mapping from source control root to local folder root.
   * 
   * @param sLocalProjectRoot
   *          File Folder which is the root for the project.
   * @param sourcePath
   *          Path for the projects root within source control eg {ProjectName}
   * 
   * @throws Exception
   *           for any errors
   */
  public void setProjectRoot(String sLocalProjectRoot, String sourcePath) throws Exception
  {
    fLocalRoot = new java.io.File(sLocalProjectRoot);
    this.localRoot = fLocalRoot.getAbsolutePath();
    this.sourceRoot = sourcePath;
    stRoot = null;// flag that root must be recalculated
    log.info(stProjectName + " has set project root " + sourcePath + " to map to " + localRoot);
  }

  /**
   * Searches for the specified source control path.
   * If found, stFolder and stFile will be set as required.
   * 
   * @param folderOrFile
   *          Specifies the server's folder/file full path specification <br>
   *          Eg.Project roots <i>Domain/Domain</i>, <i>Tools/Tools/Icons</i> <br>
   *          Sub folder/file <i>dist/Bob.jar</i>
   * 
   * @return true if found (false if not found)
   * @throws Exception
   */
  private boolean seek(String folderOrFile) throws Exception
  {
    // if stRoot is not defined yet, then find it in the view
    if (stRoot == null)
    {
      stRoot = stActiveView.getRootFolder();// root in source
      log.info("Calculating source project root (stRoot=" + sourceRoot + ") activeRoot="
          + stRoot.getFolderHierarchy() + " # " + stRoot.getDefaultPathFragment());
      if (!seek(sourceRoot))
      {// find root of source
        throw new Exception("Project's source control root is not found " + sourceRoot);
      }
      // found so stFolder points to where our project root is, make it the root
      // and set it up to point to the correct local folder
      stRoot = stFolder;
      stRoot.setAlternatePathFragment(localRoot);
      log.info("Source project root set to " + stRoot.getFolderHierarchy()
          + " and has altRoute set to " + stRoot.getAlternatePathFragment());
    }
    stFolder = stRoot;
    stFile = null;
    if (folderOrFile == null)
      return true;
    String search = folderOrFile.replace("\\", "/");
    log.info("seek: looking for '" + search + "' starting at stFolder="
        + stRoot.getFolderHierarchy());
    if (!search.endsWith("/"))
    {
      search = search + "/";
    }
    String[] folders = search.split("/");
    int bx = 0;
    while (!stFolder.getName().equalsIgnoreCase(folders[bx]))
    {
      log.info("seek: failed to match project while looking for " + folderOrFile
          + "[ stFolderName:" + stFolder.getName() + " does not match folders[0]:" + folders[0]
          + "]");
      bx++;
      if (bx > folders.length)
      {
        return false;
      }
    }
    boolean found = true;
    for (int fx = 1; fx < folders.length; fx++)
    {
      String sub = folders[fx];
      log.info("seek: folder=" + stFolder.getFolderHierarchy() + " searching for sub=" + sub);
      // scan sub folders for a match
      Folder[] stDirs = stFolder.getSubFolders();
      found = false;
      for (Folder stSubDir : stDirs)
      {
        String stName = stSubDir.getName();
        if (stName.equalsIgnoreCase(sub))
        {
          found = true;
          stFolder = stSubDir;
          log.info("seek: Found a match at " + stFolder.getFolderHierarchy());
          continue;
        }
      }
      if (!found)
      {
        log.info("seek: Could not match a folder, trying files....");
        Item[] items = stFolder.getItems(FILE);
        for (Item item : items)
        {
          File stFileItem = (File) item;
          if (stFileItem.getName().equalsIgnoreCase(sub))
          {
            stFile = stFileItem;
            log.info("seek: Found the file " + stFile.getFullName());
            return true;
          }
        }
      }
    }
    return found;
  }

  /**
   * Selects a specific folder or file within the active view to be checked out.
   * 
   * @param folderOrFile
   *          Specifies the server's folder/file full path specification <br>
   *          Eg.<i>Domain/Domain</i>, <i>Tools/Tools/Icons</i>, <i>Bob/Bob/dist/Bob.jar</i>
   * 
   * @throws Exception
   *           for any errors
   */
  public void selectPath(String folderOrFile) throws Exception
  {
    if (!seek(folderOrFile))
    {
      throw new Exception("Starteam location not found for " + folderOrFile);
    }
    // set fTgt to point to the local equivalent
    if (stFile != null)
    {
      fTgt = new java.io.File(stFile.getLocalPath());
    }
    else
    {
      fTgt = mapStFolderToDir(stFolder);
    }
    log.info("selectPath(" + folderOrFile + ") set fTgt=" + fTgt.getAbsolutePath());
  }

  /**
   * Returns a java.io.File that corresponds to the provided StarTeam Folder.
   * 
   * @param stDir
   *          StarTeam Folder
   * @return java.io.File that corresponds to the provided Folder
   */
  private java.io.File mapStFolderToDir(Folder stDir)
  {
    java.io.File f = null;
    int l = sourceRoot.length();
    String dir = stDir.getFolderHierarchy();
    if (dir.length() <= l)
    {
      f = fLocalRoot;
    }
    else
    {
      f = new java.io.File(fLocalRoot, dir.substring(l));
    }
    // log.info("ST2005:mapStFolder~ " + stDir.getFolderHierarchy() + " -> " + f.getAbsolutePath());
    return f;
  }

  /**
   * Check Out the file(s)/ Folder(s) as selected (via selectPath). <br>
   * The most recent change information will be returned.
   * 
   * @return
   * 
   * @return {@link Result} Containing latest change information.
   * @throws Exception
   *           for any errors
   */
  public Result checkOut() throws Exception
  {
    if (stFolder == null)
    {
      seek(null);// default to root
    }
    log.info("ST2005.checkOut() stFolder=" + stFolder.getFolderHierarchy()
        + " @ " + stLabel.getName()
        + " -> " + stFolder.getAlternatePathFragment());
    stOptions = new CheckoutOptions(stActiveView);
    stOptions.setOptimizeForSlowConnections(true);
    stOptions.setUpdateStatus(true);
    if (stLabelID > 0)
    {
      stOptions.setCheckoutLabelID(stLabelID);
    }
    coMan = new CheckoutManager(stActiveView);
    coMan.setOptions(stOptions);
    reportInformation = new Result(stProjectName, stLabel.getName());
    processed = 0;
    coMan.addCheckoutListener(this);
    checkOutWalker();// <-- selected
    // progMon.setAllDone();// done & kill thread
    synchronized (this)
    {
      coMan.setCanceled();
      coMan = null;
    }
    reportInformation.setRootFolder(this.fLocalRoot.getAbsolutePath());
    return reportInformation;
  }

  private void checkOutWalker() throws Exception
  {
    if (stFile != null)
    {
      queFile(stFile);
    }
    else
    {
      queFolder(stFolder);
    }
  }

  /**
   * Handle processing for a folder to be checked out.
   * <p>
   * Adds files within the folder, deals with forced checkout and removes orphan files if option is
   * set.
   * 
   * @param stFolder
   * @throws IOException
   */
  private void queFolder(Folder stFolder) throws IOException
  {
    java.io.File dir = mapStFolderToDir(stFolder);
    String path = dir.getAbsolutePath();
    ItemList fileList = stFolder.getList(FILE);
    int mx = fileList.size();
    for (int ix = 0; ix < mx; ix++)
    {
      File stf = (File) fileList.getAt(ix);
      queFile(stf);
    }
    // now recurse to sub-folders
    fileList = stFolder.getList(FOLDER);
    mx = fileList.size();
    for (int ix = 0; ix < mx; ix++)
    {
      Folder stf = (Folder) fileList.getAt(ix);
      String altPath = path + java.io.File.separator + stf.getName();
      String pf = stf.getDefaultPathFragment();
      if (pf.indexOf("..") >= 0 || pf.indexOf('\\') >= 0 || pf.indexOf('/') >= 0)
      {
        String key = stf.getFolderHierarchy();// ok
      }
      stf.setAlternatePathFragment(altPath);
      queFolder(stf);
    }
    // $log.info("Queued  (" + processed + "/" + fileCount + ") path=" + path);
  }

  /**
   * Places a file onto the check-out queue.
   * <p>
   * Skips non-modified etc depending on check-out option flags <i>force</i> and <i>zapOrphans</i>.
   */
  private void queFile(File stFile) throws IOException
  {
    processed++;
    java.io.File localFile = new java.io.File(stFile.getLocalPath(), stFile.getLocalName());
    // orphansRemove(localFile);
    checkJarCheckout(stFile);
    int status = stFile.getStatus(localFile);
    if (status == Status.UNKNOWN)
    {
      try
      {
        stFile.updateStatus(eol, useMD5);
        status = stFile.getStatus();
      }
      catch (Exception e)
      {
        log.info("Could not establish the file status for " + localFile.getAbsolutePath());
      }
    }
    String skipReason = null;
    switch (status)
    {
      case Status.MODIFIED:
        break;// changed so no reason to skip
      case Status.CURRENT:
        if (force)
        {
          break;// only skip if forced, otherwise fall through to set skip reason as CURRENT
        }
      case Status.UNKNOWN:
        skipReason = Status.name(status);
        break;
      default:
    }
    ResultItem reviewItem = null;
    if (skipReason == null || force)
    {
      try
      {
        String fileSpec = stFile.getLocalPath() + "\\" + stFile.getLocalName();
        // $log.info("FETCH  :" + fileSpec + " (" + Status.name(status) + ") " +
        // localFile.getAbsolutePath());
        File r = (File) stDefaultView.findItem(FILE_TYPE, stFile.getItemID());
        if (r != null)
        {
          coMan.checkoutTo(r, localFile);
          if (!localFile.exists())
          {
            log.info("Failed to fetch file " + fileSpec);
          }
          else
          {
            long lfs = localFile.length();
            long stfs = stFile.getSize();
            long diff = lfs;
            if (stfs != 0)
            {
              diff = Math.abs((lfs - stfs) * 100 / stfs);
            }
            if (diff > 5)
            {
              log.info("File length varies by more than 5% : local="
                  + localFile.length() + " vs StarTeam=" + stFile.getSize()
                  + " for file " + fileSpec);
            }
          }
        }
        else
        {
          reportInformation.addIssue(Issue.ISSUE_LABEL_VIEW, stFile.getFullName());
        }
        // create review information item and add to the list
        reviewItem = getReviewInfo(stFile);
        reviewItem.setStateCd(ResultItem.STATE_CHECKED_OUT);
        reportInformation.addItem(reviewItem);// checked out
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    else
    {
      // $log.info("SKIP  :" + stFile.getLocalName() + " (" + skipReason + ")");
      // create review information item and add to the list
      reviewItem = getReviewInfo(stFile);
      reviewItem.setStateCd(ResultItem.STATE_SKIPPED);
      reportInformation.addItem(reviewItem);// skipped / same
    }
    if (reviewItem != null && !reviewItem.isJavaCompliant())
    {
      reportInformation.addIssue(Issue.ISSUE_JAVA_COMPLIANCE, reviewItem.getFileSpec());
    }
    // progMon.update(processed, fileCount);
  }

  private void checkJarCheckout(File stFile)
  {
    int ix = stFile.getName().lastIndexOf(".");
    if (ix > 0)
    {
      String ext = stFile.getName().substring(ix).toLowerCase();
      if (" .jar .ear .war ".indexOf(ext) > 0)
      {
        reportInformation.addIssue(Issue.ISSUE_LABELED_JAR, stFile.getFullName());
      }
    }
  }

  /**
   * Apply a new label to the project, based on a given existing label
   * 
   * @param basedOnLabel
   *          Label to base the new label on
   * @param newLabel
   *          new label to be created
   * @param newLabelDescritpion
   *          Description for the new label
   * @throws Exception
   *           for any errors
   */
  public void createFrozenLabel(String basedOnLabel, String newLabelName, String newLabelDescritpion)
      throws Exception
  {
    Label baseLabel = findLabel(basedOnLabel);
    Label newLabel = null;
    try
    {
      newLabel = findLabel(newLabelName);
      log.info("New label '" + newLabelName + "' already exists for " + stProjectName);
    }
    catch (Exception e)
    {
      newLabel = null;
    }
    if (newLabel != null)
    {
      if (newLabel.isLocked())
      {
        log.info("Unlocking existing label '" + newLabelName + "' for " + stProjectName);
        newLabel.setLocked(false);
      }
      log.info("Removing existing label '" + newLabelName + "' from " + stProjectName);
      newLabel.remove();
    }
    stDefaultView.cloneViewLabel(baseLabel, newLabelName, newLabelDescritpion, false, true);
    log.info("Created frozen label '" + newLabelName + "' based on '" + basedOnLabel + "'");
  }

  /**
   * Search for the given label name
   * 
   * @param labelName
   *          Name of label to find
   * @return Label object for the label name
   * @throws Exception
   *           if not found
   */
  private Label findLabel(String labelName) throws Exception
  {
    if (labels == null)
    {
      labels = stDefaultView.getLabels();
    }
    for (Label l : labels)
    {
      if (l.getName().equalsIgnoreCase(labelName))
      {
        return l;
      }
    }
    throw new Exception("Label '" + labelName + "' Not found");
  }

  /**
   * Check in the file(s)/ Folder(s) as selected (via selectPath).
   * <p>
   * Must have called project,view and path already. <br>
   * The view must be the default (not labeled) - selectView(null)
   * 
   * @throws Exception
   *           for any errors
   */
  public void checkIn(String reason, String moveLabel) throws Exception
  {
    if (stFile == null)
    {
      throw new Exception("you must specify a file to be checked in -via selectPath()");
    }
    int lockStatus = Item.LockType.UNCHANGED;// new lock status
    boolean forceCheckIn = true;// false:throw exception if not 'Modified'
    boolean eol = false;// true=convert EOL chars
    boolean updateStatus = true;// true=Starteam to remember new state
    stFile.checkin(reason, lockStatus, forceCheckIn, eol, updateStatus);
    log.info("Checked in " + stFile.getLocalPath() + "\\" + stFile.getLocalName());
    if (moveLabel != null)
    {
      Label lbl = findLabel(moveLabel);
      lbl.attachToItem(stFile);
      log.info("Attached label " + lbl.getName() + " to the file " + stFile.getName());
    }
  }

  /**
   * Returns a list of changes from previous release version number
   * 
   * @param stFileSpec
   *          Specifies the file to get history for
   * @param oldLabel
   *          previous file label (from)
   * @param newLabel
   *          new file label (to)
   * @return List of ReviewInfoItem which contains change history from oldLabel to newLabel
   * @throws Exception
   *           for any errors
   */
  public List<ResultItem> getFileHistory(String stFileSpec, String oldLabel, String newLabel)
      throws Exception
  {
    ArrayList<ResultItem> revList = new ArrayList<ResultItem>();
    selectPath(stFileSpec);
    if (stFile == null)
      throw new Exception("File not found:" + stFileSpec);
    int oldRev = 0;
    int newRev = Integer.MAX_VALUE;
    if (oldLabel != null)
    {
      int stLabelIdOld = findLabel(oldLabel).getID();
      Item itmOld = stFile.getFromHistoryByLabelID(stLabelIdOld);
      oldRev = itmOld.getRevisionNumber();
    }
    if (newLabel != null)
    {
      int stLabelIdNew = findLabel(newLabel).getID();
      Item itmNew = stFile.getFromHistoryByLabelID(stLabelIdNew);
      newRev = itmNew.getRevisionNumber();
    }
    if (oldRev <= newRev)
    {
      for (Item itm : stFile.getHistory())
      {
        // skip items that are too old (before old label)
        int rev = itm.getRevisionNumber();
        if (rev <= oldRev)
        {
          continue;
        }
        // skip items that are too new (after new label)
        if (rev > newRev)
        {
          continue;
        }
        // skip items generated by Code Review process.
        String rem = itm.getComment();
        if (rem != null)
        {
          if (rem.startsWith("[Pass") || rem.startsWith("[Fail"))
          {
            continue;
          }
        }
        // add item to list
        revList.add(getReviewInfo(itm));
      }
    }
    return revList;
  }

  /**
   * Get review information for the specified star team item
   * 
   * @throws IOException
   */
  public ResultItem getReviewInfo(Item item)
  {
    com.starbase.starteam.File stFile = (com.starbase.starteam.File) item;
    ResultItem reviewItem = new ResultItem();
    reviewItem.setProjectName(stProjectName);
    reviewItem.setId(item.getID());
    reviewItem.setArchiveName(stFile.getArchiveName());
    reviewItem.setHostReference(stFile.getParentFolderHierarchy() + stFile.getName());
    int modId = item.getModifiedBy();
    String fileComment = safe(item.getComment());
    // String bad = safe((String) item.get(CODE_REVIEW_NEGATIVE_COMMENTS));
    // String good = safe((String) item.get(CODE_REVIEW_POSITIVE_COMMENTS));
    int codeReviewPass = 0;
    boolean ok = false;
    try
    {
      // codeReviewPass = (Integer) item.get(CODE_REVIEW_PASS);
      switch (codeReviewPass)
      {
        case 100:
          ok = false;
          break;
        case 101:
          ok = true;
          break;
        default:
      }
      reviewItem
          .setLocalFile(new java.io.File(stFile.getLocalPath() + "\\" + stFile.getLocalName()));
    }
    catch (Exception e1)
    {
      e1.printStackTrace();
    }
    long modifiedTime = item.getModifiedTime().getLongValue();
    // long reviewedTime = item.getOLEDate(CODE_REVIEW_DATE).getLongValue();
    // reviewItem.setReviewedTime(reviewedTime);
    reviewItem.setModifiedTime(modifiedTime);
    // long reviewAge = modifiedTime - reviewedTime;
    // if ( reviewAge > 1000 )
    // {
    // reviewItem.setComments("[DATE] " + safe(item.getComment()));
    // reviewItem.setReviewCd(ResultItem.REVIEW_REQUIRED);
    // }
    // else
    // {
    // // file has been reviewed, if it failed then log log it
    // if ( !ok )
    // {
    // reviewItem.setComments("[FAIL] " + bad);// why it failed review
    // reviewItem.setReviewCd(ResultItem.REVIEW_FAIL);
    // }
    // else
    // {
    // reviewItem.setComments("[PASS] " + good);
    // reviewItem.setReviewCd(ResultItem.REVIEW_PASS);
    // }
    // }
    reviewItem.setModifierComment(safe(fileComment));
    reviewItem.setModifierName(stServer.getUser(modId).getName());
    try
    {
      reviewItem.setFilePath(stFile.getLocalPath());
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    reviewItem.setFileName(stFile.getName());
    // more information
    reviewItem.setBranchRevisionNo(stFile.getDotNotation());
    reviewItem.setRevisionNo(stFile.getRevisionNumber() + 1);
    reviewItem.setArchiveName(stFile.getArchiveName());
    if (stLabel != null)
    {
      reviewItem.setLabel(stLabel.getName());
    }
    else
    {
      reviewItem.setLabel("");
    }
    try
    {
      int lockerID = stFile.getLocker();
      if (lockerID < 0)
      {
        reviewItem.setLockedByID(-1);
        reviewItem.setLockedBy("");
      }
      else
      {
        reviewItem.setLockedByID(lockerID);
        User locker = stServer.getUser(lockerID);
        if (locker == null)
        {
          reviewItem.setLockedBy("User_" + lockerID);
        }
        else
        {
          reviewItem.setLockedBy(locker.getName());
        }
      }
    }
    catch (Exception e)
    {
      // e.printStackTrace();
    }
    // System.out.println(reviewItem);
    return reviewItem;
  }

  /** Return xml safe version of text */
  private String safe(String text)
  {
    text = text.replaceAll("<", "&lt;");
    text = text.replaceAll(">", "&gt;");
    text = text.replaceAll("\"", "'");
    return text;
  }

  // /**
  // * Obtain project history between two labels
  // * @param projectLocation String project location in StarTeam {project}/{path}
  // * @param oldLabel Old label (start of change history)
  // * @param newLabel New label (end of change history)
  // * @return {@link SourceControlResults} which contains all history items
  // * @throws Exception
  // */
  // public Result getProjectChanges(
  // String oldLabel,
  // String newLabel,
  // IProgressListener progress)
  // throws Exception
  // {
  // progressListener = progress;
  // reportInformation = new Result(stProjectName, oldLabel + "-" + newLabel);
  // reportInformation.setFromLabel(oldLabel);
  // reportInformation.setToLabel(newLabel);
  // labels = null;
  // stLabelIdOld = findLabel(oldLabel).getID();
  // stLabelIdNew = findLabel(newLabel).getID();
  // processed = 0;
  // getFolderHistory(stDefaultView.getRootFolder());
  // progress.setAllDone();
  // reportInformation.setFileCount(processed);
  // return reportInformation;
  // }
  //
  // private void getFolderHistory(Folder stFolder)
  // {
  // for (Item itm : stFolder.getItems(FILE))
  // {
  // getFileHistory((File) itm);
  // updateProgressListener();
  // }
  // for (Item itm : stFolder.getItems(FOLDER))
  // {
  // getFolderHistory((Folder) itm);
  // }
  // }
  private void getFileHistory(File stFile)
  {
    int oldRev = 0;
    int newRev = Integer.MAX_VALUE;
    Item itmOld = stFile.getFromHistoryByLabelID(stLabelIdOld);
    Item itmNew = stFile.getFromHistoryByLabelID(stLabelIdNew);
    if (itmOld == null && itmNew == null)
    {
      // not involved in either label so ignore it
      return;
    }
    processed++;
    if (itmOld == null && itmNew != null)
    {
      // new item
      ResultItem ri = getReviewInfo(itmNew);
      ri.setHistoryCd(ResultItem.HISTORY_NEW);
      reportInformation.addItem(ri);
      return;
    }
    if (itmOld != null && itmNew == null)
    {
      // item removed
      ResultItem ri = getReviewInfo(itmOld);
      ri.setHistoryCd(ResultItem.HISTORY_REMOVED);
      reportInformation.addItem(ri);
      return;
    }
    // both labels are attached so check for history from old to new
    oldRev = itmOld.getRevisionNumber();
    newRev = itmNew.getRevisionNumber();
    if (oldRev == newRev)
    {
      // no changes
      ResultItem ri = getReviewInfo(itmOld);
      ri.setHistoryCd(ResultItem.HISTORY_SAME);
      reportInformation.addItem(ri);
      return;
    }
    int histCd = ResultItem.HISTORY_CHANGED;
    int begRev = oldRev;
    int endRev = newRev;
    if (oldRev > newRev)
    {
      histCd = ResultItem.HISTORY_REVERSED;
      begRev = newRev;
      endRev = oldRev;
    }
    // has both, so fetch history from old to new
    for (Item itm : stFile.getHistory())
    {
      // skip items that are too old (before old label)
      int rev = itm.getRevisionNumber();
      if (rev < begRev)
      {
        continue;
      }
      // skip items that are too new (after new label)
      if (rev > endRev)
      {
        continue;
      }
      // add item to list
      ResultItem riChange = getReviewInfo(itm);
      riChange.setHistoryCd(histCd);
      reportInformation.addItem(riChange);
    }
  }

  /** ================================================== CheckOutListener ====== **/
  /** Implements CheckoutListener.onNotifyProcess */
  public void onNotifyProgress(CheckoutEvent coEvt)
  {
    if (!useWalker)
    {
      CheckoutProgress progress = coEvt.getCheckoutManager().getProgress();
      int co = progress.getTotalFilesCheckedOut();
      int tot = co + progress.getTotalFilesRemaining();
      // progMon.update(co, tot);
    }
  }

  /** Implements CheckoutListener.onStartFile */
  public void onStartFile(CheckoutEvent coEvt)
  {
    com.starbase.starteam.File stF = coEvt.getCurrentFile();
    java.io.File cwF = coEvt.getCurrentWorkingFile();
    if (null != cwF)
    {
      String dest = cwF.getAbsolutePath();
      if (dest != null && !dest.startsWith(localRoot))
      {
        String key = stF.getParentFolderHierarchy();
        reportInformation.addIssue(Issue.ISSUE_FORCED_FOLDER, key + " => " + dest);
      }
    }
  }
}
