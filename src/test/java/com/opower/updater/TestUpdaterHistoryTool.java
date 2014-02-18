package com.opower.updater;

import org.junit.Test;
import org.kiji.schema.tools.BaseTool;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for {@link com.opower.updater.UpdaterHistoryTool}.
 *
 * @author felix.trepanier
 */
public class TestUpdaterHistoryTool extends UpdaterToolTest {

    @Test
    public void testDisplayTableHistory() throws Exception {
        runTool(createTool(), "--table=" + tableURI);

        assertEquals(BaseTool.SUCCESS, runTool(historyTool(), "--table=" + tableURI));

        assertEquals(5, mToolOutputLines.length);
        assertTrue(mToolOutputLines[1].startsWith("History for table"));
        assertTrue(mToolOutputLines[mToolOutputLines.length - 1].contains("CREATE TABLE " + TABLE_NAME));
    }

    private UpdaterHistoryTool historyTool() {
        return new UpdaterHistoryTool(fakeUpdaterLocker);
    }

    private UpdaterCreateTool createTool() {
        return new UpdaterCreateTool(fakeUpdaterLocker);
    }
}
