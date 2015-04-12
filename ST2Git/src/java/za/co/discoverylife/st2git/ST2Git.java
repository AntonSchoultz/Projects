package za.co.discoverylife.st2git;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;
import za.co.discoverylife.appcore.util.Convert;
import za.co.discoverylife.appcore.util.DebugUtil;
import za.co.discoverylife.st2git.commit.CommitGit;
import za.co.discoverylife.st2git.commit.ICommitter;
import za.co.discoverylife.st2git.convert.ConvertAll;
import za.co.discoverylife.st2git.filter.BaseFilter;
import za.co.discoverylife.st2git.filter.IFilter;
import za.co.discoverylife.st2git.filter.ListFilter;
import za.co.discoverylife.st2git.host.ServerSpecification;
import za.co.discoverylife.st2git.starteam.StarteamClient2005;
import za.co.discoverylife.st2git.util.ClassHelper;
import za.co.discoverylife.st2git.util.FileHelper;
import za.co.discoverylife.st2git.util.PrintFile;

public class ST2Git
{
  
  static File fRoot;// local working folder (Git work area)
  static ServerSpecification ss;// specifies the StarTeam server
  static ICommitter committer;
  static PrintFile pfError;
  
  ArrayList<Reference> refs;// list of references
  StarteamClient2005 client;
  ConvertAll cvtAll;
  ListFilter filtering;

  /**
   * Main 
   */
  public static void main(String[] args)
  {
    ST2Git cvt = new ST2Git();
    try
    {
      File root = ClassHelper.findApplicationHome();
      File stJar = new File(root, "lib/starteam-8.0.jar");
      ClassHelper.pathAddJar(stJar);
      cvt.convert();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }finally{
      if(ST2Git.pfError!=null){
        try
        {
          ST2Git.pfError.close();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    }
    System.out.println("======= DONE! =========");
  }
   
  /**
   * Configure various parts of the converter
   */
  public void configure(String ymdCutOff)
  {
    System.out.println("*** Configure");
    //---- the root working folder
    fRoot = new File("D:\\ST2GitRoot");
    pfError = new PrintFile( new File(fRoot,"ErrorLog.txt"));
    //---- star team details
    ss = new ServerSpecification("life_starteam", 49201, "ANTON11", "meatrats");
    //---- filtering
    filtering = new ListFilter();
    filtering.setCutOffDate(ymdCutOff);
    filtering.addAllFilters(BaseFilter.LIFE);
    //---- Committer
    //committer = new CommitZip(fRoot);
    committer = new CommitGit(fRoot);
  }
  
  /**
   * Do the conversion run
   */
  public void convert() throws Exception{
    long begin = System.currentTimeMillis();
    configure("2015/03/01");// configure server parameters etc
    scan();// scan StarTeam and build up work list in refs
    System.out.println("Compleated Scan in " + Convert.mSecToHMS(System.currentTimeMillis()-begin) );
    doReport();
    long begin2 = System.currentTimeMillis();
    cvtAll.execute();
    System.out.println("Compleated convertion in " + Convert.mSecToHMS(System.currentTimeMillis()-begin2) );
    System.out.println("Compleated Scan+Convert in " + Convert.mSecToHMS(System.currentTimeMillis()-begin) );
  }

  /**
   * Scans StarTeam for all matching projects and qualifying views and labels.
   * Builds up a list of references for work to be done, using filters.
   */
  public void scan()
  {
    System.out.println("*** Scan");
    cvtAll = new ConvertAll();
    try
    {
      // establish a connection with StarTeam
      connectStarTeam();
      // == build up a list of references to be converted
      refs = new ArrayList<Reference>();
      TreeSet<String> projList = client.$listAllProjects();
      HashMap<String, Long> mapLabelDate = new HashMap<String, Long>();
      for (String projectName : projList)
      {
        if (!filtering.include(projectName))
        {
          continue;
        }
        IFilter activeFilter = filtering.getActiveFilter();
        if(activeFilter==null){
          continue;// no active filter available for this project
        }
        System.out.println(projectName +" ("+activeFilter.getClass().getSimpleName()+")");
        try
        {
          client.$selectProject(projectName);
          ArrayList<String> listViews = client.$listViews();
          for (String viewName : listViews)
          {
            System.out.println("\t-" + viewName);
            client.$selectView(viewName);
            ArrayList<Reference> lstLblRefs = client.$listLabels();
            if (lstLblRefs.size() > 0)
            {
              for (Reference ref : lstLblRefs)
              {
                if (activeFilter.check(ref))
                {
                  refs.add(ref);
                  Long dt = mapLabelDate.get(ref.key);
                  if (dt == null)
                  {
                    mapLabelDate.put(ref.key, ref.timestamp);
                  }
                  else
                  {
                    if (ref.timestamp.compareTo(dt) > 0)
                    {
                      mapLabelDate.put(ref.key, ref.timestamp);
                    }
                  }
                }//end-if filter selected
              }// next selected label
            }else{
              System.out.println("\t\t! " + viewName +" has no active labels !");
            }// end if selected
          }// next view
        }
        catch (Exception e)
        {
          error("Error scanning project "+projectName,e);
        }
        
      }// next project
       // === fix timestamps
      for (Reference ref : refs)
      {
        Long dt = mapLabelDate.get(ref.key);
        if (dt != null)
        {
          ref.timestamp = dt;
        }
      }
      disconnectStarTeam();
      // === now sort the work to be done
      Collections.sort(refs, new ReferenceByRepoKey());
      for (Reference ref : refs)
      {
        cvtAll.addReference(ref);
      }     
      StringBuilder sb = new StringBuilder();
      cvtAll.toXml(sb, "");
      FileHelper.fileWrite(sb.toString(), new File(fRoot,"list.xml"));
    }
    catch (Exception e)
    {
      error("SCAN Error",e);
    }
  }

  /** Returns the Star Team host and credentials details */
  public static ServerSpecification getStarteamHost(){
    return ss;
  }
  
  /** returns the working folder */
  public static File getWorkingRoot(){
    return fRoot;
  }
  
  /** Returns the committer to be used */
  public static ICommitter getCommiter(){
    return committer;
  }
  
  public static void error(String s,Exception e){
    try
    {
      String msg = s +" : "+e.getMessage();
      System.err.println(msg);
      e.printStackTrace();
      
      pfError.println(msg);
      pfError.println(DebugUtil.getStackMessage(e));
      pfError.println();
    }
    catch (Exception e1)
    {
      e1.printStackTrace();
    }
  }

  /** Generate a report with the results of the scan */
  public void doReport()
  {
    System.out.println("*** doReport");
    try
    {
      PrintFile pf = new PrintFile( new File(fRoot,"ST2GitReport.txt"));
      reportTaskList(pf);
      reportSkipped(pf);
      pf.close();
    }
    catch (Exception e)
    {
      error("Error generating report",e);
    }
  }
  
  /** Add a list of convert tasks to the report */
  public void reportTaskList(PrintFile pf) throws Exception
  {
    // === break into groups (by key)
    pf.println("+-----------------------------+");
    pf.println("| TASK LIST                   |");
    pf.println("+-----------------------------+");
    pf.println();
    String preRepo = "";
    String preKey = "";
    for (Reference ref : refs)
    {
      if (ref.repo.compareTo(preRepo) != 0)
      {
        preRepo = ref.repo;
        pf.println("[" + preRepo+"]");
        preKey = "";
      }
      if (ref.key.compareTo(preKey) != 0)
      {
        preKey = ref.key;
        pf.println("\t" + ref.gitInfo());
      }
      pf.println("\t\t" + ref.shortString());
    }
    pf.println();
    pf.println();
  }
  
  /** Add a list of skipped projects to the report */
  public void reportSkipped(PrintFile pf) throws Exception{
    pf.println("+-----------------------------+");
    pf.println("| REJECTED / SKIPPED PROJECTS |");
    pf.println("+-----------------------------+");
    pf.println();
    for (String projName : filtering.getSkipped())
    {
      pf.println(projName);
    }
    pf.println();
    pf.println();
  }

  /** Connect to Star Team */
  public void connectStarTeam() throws Exception
  {
    client = new StarteamClient2005();
    client.setServer(ss);
    client.setCredentials(ss);
    client.connect();
  }

  /** Disconnect from Star Team */
  public void disconnectStarTeam() throws Exception
  {
    client.disconnect();
  }

}
