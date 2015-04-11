package za.co.discoverylife.desktop.host;

/** Holds credentials for a host connection<ul>
 *  <li>getUser() - returns user log in name
 *  <li>getPassword() - returns password for the connection
 *  <li>hasUser() - returns true if user is non-blank
 *  <ul>
 *  
 * @author anton11
 *
 */
public interface ICredentials
{

  /** Return user id */
  public String getUser();

  /** Return user's password */
  public String getPassword();

  /** returns true if user is non-blank */
  public boolean hasUser();

}
