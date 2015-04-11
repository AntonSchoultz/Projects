package za.co.discoverylife.appcore.table;

/** Exception for invalid column index/name */
public class InvalidColumnException extends RuntimeException
{
	private static final long serialVersionUID = -1743737999304066789L;

	public InvalidColumnException() {
		super();
	}

	public InvalidColumnException(String message) {
		super(message);
	}

	public InvalidColumnException(Throwable cause) {
		super(cause);
	}

	public InvalidColumnException(String message, Throwable cause) {
		super(message, cause);
	}

}
