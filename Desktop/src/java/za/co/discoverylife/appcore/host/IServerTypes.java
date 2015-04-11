/**
 * IServerTypes.java
 */
package za.co.discoverylife.appcore.host;

/**
 * Defines the server types know to Bob. <ol start=0>
 * <li>Unknown
 * <li>Starteam8
 * <li>Starteam14
 * <li>Weblogic
 * <li>JBoss
 * </ul>
 * 
 * @author Anton Schoultz (2014)
 */
public interface IServerTypes
{
  public static final String[] SERVER_TYPES = {
      "Unknown", "Starteam8", "Starteam14", "Weblogic", "JBoss"
  };

  public static final int SERVER_UNKNOWN = 0;
  public static final int SERVER_STARTEAM_8 = 1;
  public static final int SERVER_STARTEAM_14 = 2;
  public static final int SERVER_WEBLOGIC = 3;
  public static final int SERVER_JBOSS = 4;
}
