package za.co.discoverylife.appcore.field;

/**
 * Exception for problems with accessing fields directly
 * 
 * @author anton11
 */
public class FieldAccessException
		extends Exception
{
	private static final long serialVersionUID = 8127298875949175666L;

	public FieldAccessException() {
		super();
	}

	public FieldAccessException(String message, Throwable cause) {
		super(message, cause);
	}

	public FieldAccessException(String message) {
		super(message);
	}

	public FieldAccessException(Throwable cause) {
		super(cause);
	}

}
