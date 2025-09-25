package org.unipus.exceptions;

public class UnipusHelperException extends RuntimeException{
    public UnipusHelperException() {
        super();
    }

    public UnipusHelperException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnipusHelperException(String message) {
        super(message);
    }

    public UnipusHelperException(Throwable cause) {
        super(cause);
    }
}
