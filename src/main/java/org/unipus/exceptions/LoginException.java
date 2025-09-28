package org.unipus.exceptions;

/* 这里可以看，因为什么也没有 o(*￣▽￣*)o */

public class LoginException extends UnipusHelperException{
    public LoginException() {
        super();
    }

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginException(String message) {
        super(message);
    }

    public LoginException(Throwable cause) {
        super(cause);
    }
}
