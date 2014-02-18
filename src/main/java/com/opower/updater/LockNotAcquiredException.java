package com.opower.updater;

/**
 * Exception thrown when the lock could not be acquired.
 *
 * @author felix.trepanier
 */
public class LockNotAcquiredException extends Exception {

    public LockNotAcquiredException(String message) {
        super(message);
    }

    public LockNotAcquiredException(Throwable cause) {
        super(cause);
    }
}
