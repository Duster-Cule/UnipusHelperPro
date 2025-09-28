package org.unipus.exceptions;

/* 这里可以看，因为什么也没有 o(*￣▽￣*)o */

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
