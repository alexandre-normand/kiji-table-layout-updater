package com.opower.updater.operation;

/**
 * Exception thrown when a table does not exist but it should.
 *
 * @author felix.trepanier
 */
public class TableDoesNotExistException extends RuntimeException {
    private final String tableName;

    public TableDoesNotExistException(String tableName, String message) {
        super(message);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
