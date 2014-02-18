package com.opower.updater.operation;

/**
 * Exception thrown when the table already exists but it shouldn't.
 *
 * @author felix.trepanier
 */
public class TableAlreadyExistException extends RuntimeException {

    private final String tableName;

    public TableAlreadyExistException(String tableName, String message) {
        super(message);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
