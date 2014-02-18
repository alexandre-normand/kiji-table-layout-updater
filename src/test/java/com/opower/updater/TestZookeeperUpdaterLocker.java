package com.opower.updater;

import org.junit.Test;
import org.kiji.schema.KijiURI;
import org.kiji.schema.util.ZooKeeperTest;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

/**
 * Tests for {@link com.opower.updater.ZookeeperUpdaterLocker}.
 *
 * @author felix.trepanier
 */
public class TestZookeeperUpdaterLocker extends ZooKeeperTest {
    private static final int MAX_WAIT_TIME = 100;
    private static final TimeUnit MAX_WAIT_TIME_UNIT = TimeUnit.MILLISECONDS;

    @Test
    public void testLockCantBeAcquiredIfInUsed() throws Exception {
        final KijiURI kijiURI = KijiURI.newBuilder("kiji://" + getZKAddress() + "/instance").build();

        ZookeeperUpdaterLocker lockerA = new ZookeeperUpdaterLocker();
        ZookeeperUpdaterLocker lockerB = new ZookeeperUpdaterLocker();

        // lockerA gets the lock
        UpdaterLocker.AcquiredLock lockA = lockerA.acquireLock(kijiURI, MAX_WAIT_TIME, MAX_WAIT_TIME_UNIT);

        // lockerB must not acquire the lock
        try {
            lockerB.acquireLock(kijiURI, MAX_WAIT_TIME, MAX_WAIT_TIME_UNIT);
            fail();
        }
        catch (LockNotAcquiredException ex) {
            // ok!
        }

        // lockerA releases the lock
        lockA.release();

        // lockerB gets it
        UpdaterLocker.AcquiredLock lockB = lockerB.acquireLock(kijiURI, MAX_WAIT_TIME, MAX_WAIT_TIME_UNIT);

        lockB.release();
    }

    @Test
    public void testLockingOnDifferentInstances() throws Exception {
        ZookeeperUpdaterLocker lockerA = new ZookeeperUpdaterLocker();
        ZookeeperUpdaterLocker lockerB = new ZookeeperUpdaterLocker();

        // Both locker should obtain a lock.
        UpdaterLocker.AcquiredLock lockA = lockerA.acquireLock(KijiURI.newBuilder("kiji://" + getZKAddress() + "/A").build(),
                MAX_WAIT_TIME, MAX_WAIT_TIME_UNIT);

        UpdaterLocker.AcquiredLock lockB = lockerB.acquireLock(KijiURI.newBuilder("kiji://" + getZKAddress() + "/B").build(),
                MAX_WAIT_TIME, MAX_WAIT_TIME_UNIT);

        lockA.release();
        lockB.release();
    }

}
