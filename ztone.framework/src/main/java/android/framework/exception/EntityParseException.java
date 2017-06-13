package android.framework.exception;

public class EntityParseException extends Throwable {

	private static final long serialVersionUID = -3387516993124229948L;

	/**
	 * Constructs a new {@code Exception} that includes the current stack trace.
	 */
	public EntityParseException() {
		super();
	}

	/**
	 * Constructs a new {@code Exception} with the current stack trace and the
	 * specified detail message.
	 * 
	 * @param detailMessage
	 *            the detail message for this exception.
	 */
	public EntityParseException(String detailMessage) {
		super(detailMessage);
	}

	/**
	 * Constructs a new {@code Exception} with the current stack trace, the
	 * specified detail message and the specified cause.
	 * 
	 * @param detailMessage
	 *            the detail message for this exception.
	 * @param throwable
	 *            the cause of this exception.
	 */
	public EntityParseException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	/**
	 * Constructs a new {@code Exception} with the current stack trace and the
	 * specified cause.
	 * 
	 * @param throwable
	 *            the cause of this exception.
	 */
	public EntityParseException(Throwable throwable) {
		super(throwable);
	}
}
