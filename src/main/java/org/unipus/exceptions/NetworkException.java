package org.unipus.exceptions;

/* 这里可以看，因为什么也没有 o(*￣▽￣*)o */

public class NetworkException extends UnipusHelperException{
    public NetworkException() {
        super();
    }

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }
}
