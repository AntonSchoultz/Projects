package za.co.discoverylife.st2git.host;



/**
 * Holds details for a server connection.
 * URl, port, user and password.
 * 
 * @author anton11
 */
public class ServerSpecification
    implements IServerConnection, IServerTypes
    , Comparable<ServerSpecification>
{
  private String url = "Life_Starteam";
  private int port = 49201;
  private String user = "";
  private String password = "";
  private int serverTypeID = SERVER_STARTEAM_8;

  /**
   * CONSTRUCT default specification for connection to starteam server
   */
  public ServerSpecification()
  {
    user = System.getProperty("user.name");
  }

  /**
   * CONSTRUCT starteam connection specification with provided details
   * 
   * @param url
   *          Starteam server host name (Life_Starteam)
   * @param port
   *          Starteam host port (49201)
   * @param user
   *          Starteam user id (
   * @param password
   */
  public ServerSpecification(String url, int port, String user, String password)
  {
    super();
    setUrl(url);
    setPort(port);
    setUser(user);
    setPassword(password);
  }

  public void setServer(String url, int port)
  {
    setUrl(url);
    setPort(port);
  }

  public void setCredentials(String user, String password)
  {
    setUser(user);
    setPassword(password);
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = (url != null) ? url : "Life_Starteam";
  }

  public int getPort()
  {
    return port;
  }

  public void setPort(int port)
  {
    this.port = (port > 0) ? port : 49201;
  }

  public String getUser()
  {
    return user;
  }

  public void setUser(String user)
  {
    this.user = (user != null) ? user.trim() : System.getProperty("user.name");
  }

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = (password != null) ? password : "";
  }

  /** Returns {user}@{host}:{port} */
  public String toString()
  {
    return user + "@" + url + ":" + port
    //+ " (" + password + ")"
    ;
  }

  public boolean hasUser()
  {
    return user != null && user.length() > 0;
  }

  public void setServer(IServer server)
  {
    url = server.getUrl();
    port = server.getPort();
  }

  public void setCredentials(ICredentials credentials)
  {
    user = credentials.getUser();
    password = credentials.getPassword();
  }

  public void setServerConnection(IServerConnection connection)
  {
    setServer(connection);
    setCredentials(connection);
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(ServerSpecification o)
  {
    if ( o == null )
      return 1;// >
    int n = url.compareTo(o.url);
    if ( n != 0 )
      return n;
    n = port - o.port;
    if ( n != 0 )
      return n;
    n = user.compareTo(o.user);
    if ( n != 0 )
      return n;
    n = password.compareTo(o.password);
    return n;
  }

   /**
   * @return the serverTypeID
   */
  public int getServerTypeID()
  {
    return serverTypeID;
  }

  /** 
   * Sets serverTypeID
   * @param serverTypeID The type of server {@link IServerTypes}
   */
  public void setServerTypeID(int serverTypeID)
  {
    this.serverTypeID = serverTypeID;
  }

}
