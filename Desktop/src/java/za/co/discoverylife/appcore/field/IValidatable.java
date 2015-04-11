package za.co.discoverylife.appcore.field;
/** Defines an object as being validate-able */
public interface IValidatable {

	/** 
	 * Throws an Exception if the object's values are invalid */
	public void validate() throws Exception;
}
