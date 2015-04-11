package za.co.discoverylife.appcore.host;

import za.co.discoverylife.desktop.host.ICredentials;
import za.co.discoverylife.desktop.host.IServer;

/**
 * Defines functions which allow a client to connect to a server<ul>
 * <li>setServer(IServer host)
 * <li>setCredentials(ICredentials credentials)
 * </ul>
 * 
 * @author Anton Schoultz (2013)
 *
 */
public interface IClient
{

  /** 
   * Sets the address and port of the server.
   * 
   * @param host IServer - holds host address and port
   * @throws Exception for any errors
   */
  public void setServer(IServer host) throws Exception;

  /** 
   * Sets the logOn credentials to be used to access the server.
   * 
   * @param credentials ICredentials - holds logInUser and Password
   * @throws Exception for any errors
   */
  public void setCredentials(ICredentials credentials) throws Exception;

}
