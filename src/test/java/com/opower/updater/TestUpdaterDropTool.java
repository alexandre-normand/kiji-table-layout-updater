package com.opower.updater;

import org.junit.Test;
import org.kiji.schema.tools.BaseTool;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for {@link com.opower.updater.UpdaterDropTool}.
 *
 * @author felix.trepanier
 */
public class TestUpdaterDropTool extends UpdaterToolTest {

    @Test
    public void testDeleteFailsIfTableDoesNotExist() throws Exception {
        assertEquals(BaseTool.FAILURE, runTool(dropTool(), "--table=" + tableURI, "--interactive=false"));

        checkLastPrintedLineIsAnError();
    }

    @Test
    public void testDelete() throws Exception {
        runTool(createTool(), "--table=" + tableURI);

        assertTrue(getKiji().getMetaTable().tableExists("test"));
        assertEquals(BaseTool.SUCCESS, runTool(dropTool(), "--table=" + tableURI, "--interactive=false"));
        assertFalse(getKiji().getMetaTable().tableExists("test"));
    }

    @Test
    public void testCreateThenDropThenCreate() throws Exception {
        runTool(createTool(), "--table=" + tableURI);
        runTool(dropTool(), "--table=" + tableURI, "--interactive=false");

        runTool(createTool(), "--table=" + tableURI);
        assertTrue(getKiji().getMetaTable().tableExists(TABLE_NAME));
        assertTestTableComplete();
    }

    private UpdaterDropTool dropTool() {
        return new UpdaterDropTool(fakeUpdaterLocker);
    }

    private UpdaterCreateTool createTool() {
        return new UpdaterCreateTool(fakeUpdaterLocker);
    }
}
