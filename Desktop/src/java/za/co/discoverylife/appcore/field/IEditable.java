package za.co.discoverylife.appcore.field;

/**
 * Defines an object as editable.
 * <dl>
 * <dt>undo()
 * <dd>Set edit value from the object field value
 * <dt>commit()
 * <dd>Commit the edit value to the object
 * <dt>reset()
 * <dd>Set edited value to the initial/default values provided in FieldInfo
 * </dl>
 * 
 * @author anton11
 */
public interface IEditable {
	/** set edit value from the object field value */
	public void undo();

	/** Commit the edit value to the object */
	public void commit();

	/** Set edited value to the initial/default values provided in FieldInfo */
	public void reset();
}
