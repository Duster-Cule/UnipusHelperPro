package org.unipus.exceptions;

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
