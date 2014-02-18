package com.opower.updater;

import com.google.common.io.Closeables;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.kiji.schema.KijiURI;

import java.util.concurrent.TimeUnit;

/**
 * Updater lock implementation based on Curator {@link org.apache.curator.framework.recipes.locks.InterProcessMutex}.
 *
 * @author felix.trepanier
 */
public class ZookeeperUpdaterLocker implements UpdaterLocker {
    private static final String PATH = "/kiji/schema/updater/lock";
    private static final Integer BASE_SLEEP_TIME_MS = 1000;
    private static final Integer MAX_RETRY = 3;

    /**
     * {@inheritDoc}
     */
    @Override
    public AcquiredLock acquireLock(KijiURI kijiURI, int maxWaitTime, TimeUnit timeUnit) throws LockNotAcquiredException {
        String zookeeperQuorum = StringUtils.join(kijiURI.getZookeeperQuorumOrdered(), ",");
        String zookeeperPort = Integer.toString(kijiURI.getZookeeperClientPort());
        CuratorFramework curatorFramework =
                CuratorFrameworkFactory.newClient(zookeeperQuorum + ":" + zookeeperPort,
                        new ExponentialBackoffRetry(BASE_SLEEP_TIME_MS, MAX_RETRY));
        curatorFramework.start();
        InterProcessLock mutex;

        mutex = new InterProcessMutex(curatorFramework, PATH + "/" + kijiURI.getInstance());
        try {
            acquireOrThrow(mutex, maxWaitTime, timeUnit);
            return new InternalAcquiredLock(mutex, curatorFramework);
        }
        catch (LockNotAcquiredException ex) {
            curatorFramework.close();
            throw ex;
        }
        catch (Exception ex) {
            curatorFramework.close();
            throw new LockNotAcquiredException(ex);
        }
    }

    /**
     * AcquiredLock implementation that releases the underlying {@link org.apache.curator.framework.recipes.locks.InterProcessLock}
     * and closes the {@link org.apache.curator.framework.CuratorFramework}.
     */
    private static final class InternalAcquiredLock implements AcquiredLock {
        private final InterProcessLock lock;
        private final CuratorFramework curatorFramework;

        private InternalAcquiredLock(InterProcessLock lock, CuratorFramework curatorFramework) {
            this.lock = lock;
            this.curatorFramework = curatorFramework;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void release() throws Exception {
            try {
                lock.release();
            }
            finally {
                Closeables.close(curatorFramework, true);
            }
        }
    }

    private static void acquireOrThrow(InterProcessLock mutex, int maxWaitTime, TimeUnit timeUnit)
            throws LockNotAcquiredException {
        boolean acquired;
        try {
            acquired = mutex.acquire(maxWaitTime, timeUnit);
        }
        catch (Exception ex) {
            throw new LockNotAcquiredException(ex);
        }

        if (!acquired) {
            throw new LockNotAcquiredException("Could not acquire after " + maxWaitTime + " "
                    + timeUnit.toString().toLowerCase() + ".");
        }
    }
}
