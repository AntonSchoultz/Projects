package za.co.discoverylife.st2git.starteam;

import java.io.File;

/** 
 * Holds the review information for a file.
 * <p>
 * Comparable compares {filePath}/{fileName} only.
 *
 * @author Anton Schoultz (2013)
 */
public class ResultItem implements Comparable<ResultItem>
{
  private static final String JAVA_MANIFEST_MF = "MANIFEST.MF";
  private static final String JAVA_WEB_INF = "WEB-INF";
  private static final String JAVA_META_INF = "META-INF";

  public static final int REVIEW_REQUIRED = 0;
  public static final int REVIEW_FAIL = 1;
  public static final int REVIEW_PASS = 2;
  public static final String[] REVIEW = {
      "ToDo", "Failed", "Passed"};

  // these states are used for change reporting
  public static final int HISTORY_UNKNOWN = 0;
  public static final int HISTORY_REMOVED = 1;
  public static final int HISTORY_NEW = 2;
  public static final int HISTORY_CHANGED = 3;
  public static final int HISTORY_REVERSED = 4;
  public static final int HISTORY_SAME = 5;
  public static final String[] HISTORY = {
      "Unknown", "Removed", "New", "Modified", "Reversed", "Same"};

  public static final int STATE_UNKNOWN = 0;
  public static final int STATE_CHECKED_OUT = 1;
  public static final int STATE_SKIPPED = 2;

  public static final String[] STATE = {
      "Unknown", "Fetched", "Skipped"};

  public static final int REVIEW_MAX_AGE_MS = 60000;

  //@MetaFieldInfo(label = "Project", hint = "Project Name.", isReadOnly = true)
  private String projectName = "";

  private String filePath = "";

  private String fileName = "";

  //@MetaFieldInfo(label = "Label", hint = "Label", isReadOnly = true)
  private transient String label = "";

  // @MetaFieldInfo(label = "ID", hint = "Star Team ID.",isReadOnly=true)
  private transient int id;

  private String modifierName = "";

  private String modifierComment = "";

  //@MetaFieldInfo(label = "Comments", hint = "review comments for this file.", dropRows = 3)
  private String reviewerComment = "";

  //@MetaFieldInfo(label = "Branch Revision No", hint = "Branch revision number (1.x)", isReadOnly = true)
  private String branchRevisionNo = "";

  //@MetaFieldInfo(label = "Archive", hint = "Archive File name", isReadOnly = true)
  private transient String archiveName;

  //@MetaFieldInfo(label = "Revision", hint = "File revision number")
  private int revisionNo = 0;// content version

  private transient int lockedByID = 0;

  private transient String lockedBy = "";

  private int stateCd = STATE_UNKNOWN;
  private int reviewCd = REVIEW_REQUIRED;
  private int historyCd = HISTORY_UNKNOWN;

  private File localFile;

  private long modifiedTime;

  private long reviewedTime;

  private String hostReference;

  //@MetaFieldInfo(label = "Revision Information", hint = "Revision information", isReadOnly = true)
  private transient String info = "";

  public void setInfo()
  {
    info = "<html>" + label + " Revision=" + revisionNo + ", Branch=" + branchRevisionNo
        + (lockedBy.length() < 1 ? "" : "<b> [Locked by " + lockedBy + "]</b>");
  }

  public String getHostFolder()
  {
    int ix = hostReference.lastIndexOf("/");
    if ( ix < 0 )
    {
      ix = hostReference.lastIndexOf("\\");
    }
    if ( ix >= 0 )
    {
      return hostReference.substring(0, ix);
    }
    return hostReference;
  }

  public boolean isPass()
  {
    return reviewCd == REVIEW_PASS;
  }

  public boolean isFail()
  {
    return reviewCd == REVIEW_FAIL;
  }

  public boolean isDeleted()
  {
    return isState(HISTORY_REMOVED);
  }

  public boolean isChanged()
  {
    return isState(HISTORY_CHANGED);
  }

  public boolean isNew()
  {
    return isState(HISTORY_NEW);
  }

  public boolean isSame()
  {
    return isState(HISTORY_SAME);
  }

  /** Returns [Pass/Fail R### B#.##] review remarks (modifier remarks) */
  public String getReviewComment()
  {
    return (isPass() ? "[Pass" : "[Fail")
        + " Rev" + getRevisionNo()
        + " Brn" + getBranchRevisionNo() + "] "
        + reviewerComment
        + " (" + modifierComment + ")";
  }

  /** Returns true for code review entries */
  public boolean isCodeReview()
  {
    String rem = this.modifierComment;
    boolean res = rem.startsWith("[Pass") || rem.startsWith("[Fail");
    return res;
  }

  /** Returns true is this item matches the specified state 
   * STATE_REVIEW_REQUIRED/FAIL/PASS
   *
   * @param filterState
   * @return
   */
  public boolean isState(int filterState)
  {
    return stateCd == filterState;
  }

  public String getLocalFileSpec()
  {
    return localFile.getAbsolutePath();
  }

  public String getProjectName()
  {
    return projectName;
  }

  public void setProjectName(String projectName)
  {
    this.projectName = projectName;
  }

  public String getModifierName()
  {
    return modifierName;
  }

  public void setModifierName(String modifierName)
  {
    this.modifierName = modifierName;
  }

  public void setComments(String comments)
  {
    this.reviewerComment = comments;
  }

  public String getComments()
  {
    return reviewerComment;
  }

  public boolean isJavaCompliant()
  {
    boolean valid = true;
    // check that manifest file is in upper csae
    if ( JAVA_MANIFEST_MF.equalsIgnoreCase(fileName) )
    {
      valid &= JAVA_MANIFEST_MF.compareTo(fileName) == 0;
      //System.out.println("Check manifest " + valid + " " + fileName);
    }
    // check folders web-inf and meta-inf are upper-case
    String filePATH = filePath.toUpperCase();
    if ( filePATH.endsWith(JAVA_WEB_INF) )
    {
      valid &= (filePath.endsWith(JAVA_WEB_INF));
      //System.out.println("Check web-inf " + valid + " " + filePath);
    }
    if ( filePATH.endsWith(JAVA_META_INF) )
    {
      valid &= filePath.endsWith(JAVA_META_INF);
      //System.out.println("Check meta-inf " + valid + " " + filePath);
    }
    return valid;
  }

  /** Returns filePath + fileName */
  public String getFileSpec()
  {
    if ( filePath.endsWith("/") )
    {
      return filePath + fileName;
    }
    else
    {
      return filePath + "/" + fileName;
    }
  }

  public String getFilePath()
  {
    return filePath;
  }

  public void setFilePath(String filePath)
  {
    this.filePath = filePath == null ? "" : filePath.replace('\\', '/');
  }

  public int compareProjectName(ResultItem other)
  {
    return projectName.compareTo(other.projectName);
  }

  public int compareModifier(ResultItem other)
  {
    return modifierName.compareTo(other.modifierName);
  }

  public int compareFilePath(ResultItem other)
  {
    return filePath.compareTo(other.filePath);
  }

  public int getId()
  {
    return id;
  }

  public void setId(int id)
  {
    this.id = id;
  }

  @Override
  /** Returns full string representation of the ResultItem */
  public String toString()
  {
    return "ResultItem [projectName=" + projectName + ", localFile="
        + (localFile == null ? "null(" + fileName + ")" : localFile.getAbsolutePath()) + ", Revision=" + revisionNo
        + ", ReviewedDate=" + new java.util.Date(reviewedTime).toString() + ", BranchRevision=" + branchRevisionNo
        + ", ArchiveName=" + archiveName + ", Label=" + label + ", modifierName=" + modifierName + ", comments="
        + reviewerComment + ", " + STATE[stateCd] + ", LockedBy=" + lockedBy + "]";
  }

  /** Returns file, revision and modifier */
  public String toShortString()
  {
    return "File:" + (localFile == null ? "null(" + fileName + ")" : localFile.getAbsolutePath())
        + "\r\n\tRev." + revisionNo + " (" + branchRevisionNo + ") " + modifierName + ":" + reviewerComment;
  }

  /** Returns state, revNo, hostRef(path), modifier, revComment */
  public String toChangeString()
  {
    return STATE[stateCd]
        + "\t" + revisionNo + " (" + branchRevisionNo + ")"
        + "\t" + hostReference
        + "\t" + modifierName + ":" + reviewerComment;
  }

  public String getVersionString()
  {
    return info;
  }

  public String getModifierComment()
  {
    return modifierComment;
  }

  public void setModifierComment(String modifierComment)
  {
    this.modifierComment = modifierComment;
  }

  public String getFileName()
  {
    return fileName;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public String getBranchRevisionNo()
  {
    return branchRevisionNo;
  }

  public void setBranchRevisionNo(String branchRevisionNo)
  {
    this.branchRevisionNo = branchRevisionNo;
    setInfo();
  }

  public int getRevisionNo()
  {
    return revisionNo;
  }

  public void setRevisionNo(int revisionNo)
  {
    this.revisionNo = revisionNo;
    setInfo();
  }

  public String getArchiveName()
  {
    return archiveName;
  }

  public void setArchiveName(String archiveName)
  {
    this.archiveName = archiveName;
    setInfo();
  }

  public String getLabel()
  {
    return label;
  }

  public void setLabel(String label)
  {
    this.label = label;
    setInfo();
  }

  /** Compares by file path and name only */
  public int compareTo(ResultItem o)
  {
    String mySpec = filePath + "/" + fileName;
    ResultItem other = (ResultItem) o;
    String oSpec = other.filePath + "/" + other.fileName;
    return mySpec.compareTo(oSpec);
  }

  /** Compare the host references (same file?) */
  public int compareHostReference(ResultItem other)
  {
    // first compare to see if we're talking about the same file
    return hostReference.compareTo(other.hostReference);
  }

  /** Compare by revision number (Same revision no) */
  public int compareRevisionNo(ResultItem other)
  {
    return revisionNo - other.revisionNo;
  }

  /** Compare by ID */
  public int compareID(ResultItem other)
  {
    return id - other.id;
  }

  public File getLocalFile()
  {
    return localFile;
  }

  public void setLocalFile(File localFile)
  {
    this.localFile = localFile;
  }

  public String getLockedBy()
  {
    return lockedBy;
  }

  public void setLockedBy(String lockedBy)
  {
    this.lockedBy = lockedBy;
  }

  public int getLockedByID()
  {
    return lockedByID;
  }

  public void setLockedByID(int lockedByID)
  {
    this.lockedByID = lockedByID;
  }

  public void setReviewedTime(long reviewedTime)
  {
    this.reviewedTime = reviewedTime;
  }

  public long getReviewedTime()
  {
    return reviewedTime;
  }

  public long getModifiedTime()
  {
    return modifiedTime;
  }

  public void setModifiedTime(long modifiedTime)
  {
    this.modifiedTime = modifiedTime;
  }

  public String getHostReference()
  {
    return hostReference;
  }

  public void setHostReference(String hostReference)
  {
    this.hostReference = hostReference;
  }

  public String getStateString()
  {
    return STATE[stateCd];
  }

  public String getHistoryString()
  {
    return HISTORY[historyCd];
  }

  public String getReviewString()
  {
    return REVIEW[reviewCd];
  }

  public int getStateCd()
  {
    return stateCd;
  }

  public void setStateCd(int state)
  {
    this.stateCd = state;
  }

  public int getReviewCd()
  {
    return reviewCd;
  }

  public void setReviewCd(int reviewCd)
  {
    this.reviewCd = reviewCd;
  }

  public int getHistoryCd()
  {
    return historyCd;
  }

  public void setHistoryCd(int historyCd)
  {
    this.historyCd = historyCd;
  }

}
