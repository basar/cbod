package net.bsrc.cbod.core.exception;

@SuppressWarnings("serial")
public class CBODException extends RuntimeException {

	public CBODException() {

	}

	public CBODException(Throwable t) {
		super(t);
	}

	public CBODException(String message) {
		super(message);
	}

}
