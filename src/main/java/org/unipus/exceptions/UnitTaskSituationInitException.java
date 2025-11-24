package org.unipus.exceptions;

/* 这里可以看，因为什么也没有 o(*￣▽￣*)o */

public class UnitTaskSituationInitException extends RuntimeException {
    public UnitTaskSituationInitException(String message) {
        super(message);
    }

    public  UnitTaskSituationInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
