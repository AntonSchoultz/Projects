package za.co.discoverylife.st2git.host;

/** 
 * Holds server URL and port for a connection<ul>
 * <li>getUrl() - URL to the server
 * <li>getPort() - port to use for connection
 * <ul>
 * 
 * @author anton11
 *
 */
public interface IServer
{
  /** Return the URL to the server */
  public String getUrl();

  /** Return the port number of the server */
  public int getPort();

}
