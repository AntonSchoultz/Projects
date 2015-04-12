package za.co.discoverylife.st2git;

import za.co.discoverylife.appcore.util.DateTime;
import com.starbase.starteam.Label;

public class Reference
{
  public String stProject;
  public String stView;
  public String stLabel;
  public String stLabelDesrcription;
  public Long timestamp;
  // -- these must both be set if the reference is to be used/converted
  public String repo = null;// specifies which GIT Repository to go to (null = not selected)
  public String key = null;// used to group into batches
  // -- additional information for GIT
  public String gitBranch = null;
  public String gitLabel = null;

  public Reference(String stProject, String stView, Label lbl)
  {
    super();
    this.stProject = stProject;
    this.stView = stView;
    stLabel = lbl.getName();
    stLabelDesrcription = lbl.getDescription();
    timestamp = lbl.getRevisionTime().getLongValue();
  }

  public void setTimestamp(long timestamp)
  {
    this.timestamp = timestamp;
  }

  public String getYmdHms()
  {
    DateTime dtm = new DateTime();
    dtm.setTimeInMillis(timestamp);
    return dtm.toStringYmdHms();
  }

  public String getYmd()
  {
    DateTime dtm = new DateTime();
    dtm.setTimeInMillis(timestamp);
    return dtm.toStringYmd();
  }

  public String gitInfo()
  {
    return "Repo=" + repo + ", branch=" + gitBranch + ", label=" + gitLabel + ", date="
        + getYmdHms();
  }

  public String shortString()
  {
    return stProject + "#" + stView + "@" + stLabel + " : " + stLabelDesrcription + " "
        + getYmdHms();
  }

  public void toXml(StringBuilder sb, String pad)
  {
    sb.append(pad).append("<Reference ");
    sb.append("stProject='").append(stProject);
    sb.append("' stView='").append(stView);
    sb.append("' stLabel='").append(stLabel);
    sb.append("' />\r\n");
  }

  public String toString()
  {
    return "Reference [stProject=" + stProject + ", stView=" + stView + ", stLabel=" + stLabel
        + ", stLabelDesrcription=" + stLabelDesrcription + ", timestamp=" +getYmdHms()+ ", repo="
        + repo + ", key=" + key + "]";
  }
}
