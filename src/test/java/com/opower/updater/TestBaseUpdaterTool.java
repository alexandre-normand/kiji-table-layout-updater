package com.opower.updater;

import com.opower.updater.operation.OperationForbiddenException;
import org.junit.Before;
import org.junit.Test;
import org.kiji.schema.KijiURI;
import org.kiji.schema.tools.BaseTool;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.opower.updater.BaseUpdaterTool}.
 *
 * @author felix.trepanier
 */
public class TestBaseUpdaterTool extends UpdaterToolTest {
    private static final KijiURI KIJI_URI = KijiURI.newBuilder("kiji://localhost:2182/instance").build();

    private UpdaterLocker mockUpdateLocker;
    private UpdaterLocker.AcquiredLock mockLock;
    private DummyTool tool;

    @Before
    public void setup() throws Exception {
        mockUpdateLocker = mock(UpdaterLocker.class);
        mockLock = mock(UpdaterLocker.AcquiredLock.class);
        tool = new DummyTool(mockUpdateLocker);
        tool.setup();
    }

    @Test
    public void testShouldAcquireLockExecuteAndRelease() throws Exception {
        when(mockUpdateLocker.acquireLock(eq(KIJI_URI), anyInt(), (TimeUnit) anyObject())).thenReturn(mockLock);

        assertEquals(BaseTool.SUCCESS, runTool(tool));

        verify(mockUpdateLocker).acquireLock(eq(KIJI_URI), anyInt(), (TimeUnit) anyObject());
        verify(mockLock).release();

        assertTrue(tool.isExecuted());
    }

    @Test
    public void testIfLockIsNotAcquiredDoNotExecute() throws Exception {
        when(mockUpdateLocker.acquireLock(eq(KIJI_URI), anyInt(), (TimeUnit) anyObject()))
                .thenThrow(new LockNotAcquiredException("Ben non!"));

        assertEquals(BaseTool.FAILURE, runTool(tool));

        verify(mockUpdateLocker).acquireLock(eq(KIJI_URI), anyInt(), (TimeUnit) anyObject());
        verifyZeroInteractions(mockLock);

        assertFalse(tool.isExecuted());
    }

    @Test
    public void testIfExceptionCallReleaseOnLock() throws Exception {
        when(mockUpdateLocker.acquireLock(eq(KIJI_URI), anyInt(), (TimeUnit) anyObject())).thenReturn(mockLock);
        tool.setSimulatedException(new OperationForbiddenException("Nope, can't do that!"));

        assertEquals(BaseTool.FAILURE, runTool(tool));

        verify(mockUpdateLocker).acquireLock(eq(KIJI_URI), anyInt(), (TimeUnit) anyObject());
        verify(mockLock).release();

        assertFalse(tool.isExecuted());
    }

    private class DummyTool extends BaseUpdaterTool {
        private boolean executed = false;
        private Exception simulatedException = null;

        protected DummyTool(UpdaterLocker locker) {
            super(locker);
        }

        public boolean isExecuted() {
            return executed;
        }

        public void setSimulatedException(Exception ex) {
            simulatedException = ex;
        }

        @Override
        protected int executeToolOperation(List<String> nonFlagArgs) throws Exception {
            if (simulatedException != null) {
                throw simulatedException;
            }
            executed = true;
            return BaseTool.SUCCESS;
        }

        @Override
        protected KijiURI createKijiURI() {
            return KIJI_URI;
        }

        @Override
        public String getName() {
            return "dummy";
        }

        @Override
        public String getDescription() {
            return "Tool to use when it's freezing cold in Montreal.";
        }
    }
}
