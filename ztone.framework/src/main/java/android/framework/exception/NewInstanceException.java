package android.framework.exception;

import android.assist.Assert;

public class NewInstanceException extends Throwable {

	private static final long serialVersionUID = -3387516993124229948L;

	/**
	 * Constructs a new {@code Exception} that includes the current stack trace.
	 */
	public NewInstanceException() {
		super();
	}

	/**
	 * Constructs a new {@code Exception} with the current stack trace and the specified detail message.
	 * 
	 * @param detailMessage
	 *            the detail message for this exception.
	 */
	public NewInstanceException(String detailMessage) {
		super(getMessage(detailMessage));
	}

	/**
	 * Constructs a new {@code Exception} with the current stack trace, the specified detail message and the specified
	 * cause.
	 * 
	 * @param detailMessage
	 *            the detail message for this exception.
	 * @param throwable
	 *            the cause of this exception.
	 */
	public NewInstanceException(String detailMessage, Throwable throwable) {
		super(getMessage(detailMessage), throwable);
	}

	/**
	 * Constructs a new {@code Exception} with the current stack trace and the specified cause.
	 * 
	 * @param throwable
	 *            the cause of this exception.
	 */
	public NewInstanceException(Throwable throwable) {
		super(throwable);
	}

	private static String getMessage(String message) {
		return Assert.notEmpty(message) ? message : "new instance error!";
	}
}
