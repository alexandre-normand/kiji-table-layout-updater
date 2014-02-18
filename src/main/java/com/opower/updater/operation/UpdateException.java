package com.opower.updater.operation;

import com.opower.updater.admin.Update;

/**
 * Exception representing a failure to apply an update.
 *
 * @author felix.trepanier
 */
public class UpdateException extends RuntimeException {
    private final String tableName;
    private final Update failedUpdate;

    public UpdateException(String tableName, Update failedUpdate, Throwable cause) {
        super(cause);
        this.failedUpdate = failedUpdate;
        this.tableName = tableName;
    }

    /**
     * @return the failed update information.
     */
    public Update getFailedUpdate() {
        return failedUpdate;
    }

    /**
     * @return the table name on which the failed update was applied.
     */
    public String getTableName() {
        return tableName;
    }
}
