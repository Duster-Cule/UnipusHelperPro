package org.unipus.exceptions;

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
