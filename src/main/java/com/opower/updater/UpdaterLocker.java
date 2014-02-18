package com.opower.updater;

import org.kiji.schema.KijiURI;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Interface to obtain the updater lock to prevent multiple updater to execute at the same time on a given instance.
 *
 * @author felix.trepanier
 */
public interface UpdaterLocker {

    /**
     * Acquire the updater tool lock for a given kiji instance.
     *
     * @param kijiURI The kiji instance URI.
     * @return The lock.
     * @throws UpdaterLocker.LockNotAcquiredException
     * @throws java.io.IOException
     */
    AcquiredLock acquireLock(KijiURI kijiURI, int maxWaitTime, TimeUnit timeUnit) throws LockNotAcquiredException, IOException;

    /**
     * Simple interface to represent the updater lock and allow the release operation on it.
     */
    interface AcquiredLock {

        /**
         * Release the acquired AcquiredLock. This method should be called in a finally block.
         */
        void release() throws Exception;
    }
}
