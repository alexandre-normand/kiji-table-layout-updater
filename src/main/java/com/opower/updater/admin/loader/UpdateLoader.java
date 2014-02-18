package com.opower.updater.admin.loader;

import com.opower.updater.admin.Update;

import java.io.IOException;
import java.util.SortedSet;

/**
 * Interface for loading updates.
 *
 * @author felix.trepanier
 */
public interface UpdateLoader {
    /**
     * Load the initial update (i.e. the CREATE TABLE statement) for a given table.
     *
     * @param tableName The name of the table to load the initial update for.
     * @return The initial update.
     * @throws IOException
     */
    Update loadCreateTable(String tableName) throws IOException;

    /**
     * Load all updates for a given table.
     *
     * @param tableName The name of the table to load the updates for.
     * @return A sorted set of all updates DDL (sorted by update id).
     * @throws IOException
     */
    SortedSet<Update> loadUpdates(String tableName) throws IOException;
}
