package org.unipus.exceptions;

/* 这里可以看，因为什么也没有 o(*￣▽￣*)o */

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
