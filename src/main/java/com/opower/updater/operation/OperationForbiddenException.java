package com.opower.updater.operation;

/**
 * Exception thrown when the requested operation is forbidden (e.g. dropping the layout_update table).
 *
 * @author felix.trepanier
 */
public class OperationForbiddenException extends RuntimeException {
    public OperationForbiddenException(String message) {
        super(message);
    }
}
