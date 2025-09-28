package org.unipus.exceptions;

/* 这里可以看，因为什么也没有 o(*￣▽￣*)o */

public class TaskInitFailedException extends UnipusHelperException {
    public TaskInitFailedException(String message) {
        super(message);
    }
    public TaskInitFailedException(Throwable cause) {
        super(cause);
    }
    public TaskInitFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
