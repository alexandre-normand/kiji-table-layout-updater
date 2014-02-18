package com.opower.updater.admin.loader;

import com.opower.updater.admin.Update;

/**
 * Interface for processing updates.
 *
 * @author felix.trepanier
 */
public interface UpdateProcessor {

    /**
     * Process the update and return the transformed update.
     *
     * @param update The update to process.
     * @return The resulting update.
     */
    Update processUpdate(Update update);
}
