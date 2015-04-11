package za.co.discoverylife.appcore.field;

/** Defines an object as being copy-able via the copyTo(other) method */
public interface ICopyable
		extends Cloneable
{
	/** Copies data to the supplied object of the same kind */
	public void copyTo(Object other);
}
