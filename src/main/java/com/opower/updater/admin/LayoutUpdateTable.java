package com.opower.updater.admin;

import com.opower.updater.LayoutUpdate;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

/**
 * Interface used to interact with the LayoutUpdate table.
 *
 * @author felix.trepanier
 */
public interface LayoutUpdateTable extends Closeable {
    String TABLE_NAME = "layout_update";
    String UPDATE_LOG_FAMILY_NAME = "update_log";
    String LAYOUT_UPDATE_COLUMN_NAME = "layout_update";

    /**
     * Get the last update id applied to a specific kiji table.
     *
     * @param tableName The table name
     * @return The last update id for the given table. This method will return null if there are no entries in the
     * layout_update table for the table.
     * @throws IOException
     */
    Integer getLastUpdateIdForTable(String tableName) throws IOException;

    /**
     * Insert a new update entry in the layout_update table.
     *
     * @param tableName    The table name that was updated.
     * @param layoutUpdate The description of the update.
     * @throws IOException
     */
    void insertLayoutUpdate(String tableName, LayoutUpdate layoutUpdate) throws IOException;

    /**
     * Get the table history for a given table.
     *
     * @param tableName        The table name to get the history for.
     * @param numberOfVersions The maximum number of history version to return.
     * @return A collection of LayoutUpdate representing the latest modifications on the given table.
     * @throws IOException
     */
    Collection<LayoutUpdate> getTableHistory(String tableName, Integer numberOfVersions) throws IOException;
}
