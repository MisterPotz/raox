package ru.bmstu.rk9.rao.ui;

/**
 * This exception should be used in places which required CurrentSimulator which is now unsupported
 */
public class TodoUndefinedCallPlaceException extends IllegalStateException {
	public TodoUndefinedCallPlaceException(String additionalReason) {
		super("TodoUndefinedCallPlaceException: " + additionalReason);
	}
	
	public TodoUndefinedCallPlaceException() {
		this("");
	}
	
}
