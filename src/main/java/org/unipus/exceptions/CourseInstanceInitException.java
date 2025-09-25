package org.unipus.exceptions;

public class CourseInstanceInitException extends UnipusHelperException{
    public CourseInstanceInitException() {
        super();
    }

    public CourseInstanceInitException(String message) {
        super(message);
    }

    public CourseInstanceInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public CourseInstanceInitException(Throwable cause) {
        super(cause);
    }
}
