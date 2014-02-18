package com.opower.updater.admin.loader;

/**
 * Thrown when the table updates could not be found.
 *
 * @author felix.trepanier
 */
public class TableUpdatesNotFoundException extends RuntimeException {
    private final String tableName;

    public TableUpdatesNotFoundException(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
