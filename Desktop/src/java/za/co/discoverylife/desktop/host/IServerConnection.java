package za.co.discoverylife.desktop.host;

/** 
 *  Holds all details for a connection to a host.<ul>
 *  <li><br><b>IServer</b> (URL and port number)<ul>
 *      <li>getUrl() - URL to the server
 *      <li>getPort() - port to use for connection
 *      </ul>
 *  <li><b>ICredentials</b>Holds credentials for a host connection<ul>
 *      <li>getUser() - returns user log in name
 *      <li>getPassword() - returns password for the connection
 *      <li>hasUser() - returns true if user is non-blank
 *      </ul>
 *  <ul>
 * 
 * @author Anton Schoultz (2013)   
 */
public interface IServerConnection
    extends IServer, ICredentials
{

}
