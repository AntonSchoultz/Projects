package za.co.discoverylife.appcore.task;
/**
 * Used to define classes that can respond to a 
 * special hyper-link from within a viewed page.
 * 
 * The link should include '[Action:actionName?param=value&p2=v2]...
 * 
 * the parameter parts are optional.
 * 
 * @author Anton Schoultz
 */
public interface ILinkAction
{
	
	/** Perform the required action **/
	public void doAction(LinkTask taskRequest) throws Exception;
}
