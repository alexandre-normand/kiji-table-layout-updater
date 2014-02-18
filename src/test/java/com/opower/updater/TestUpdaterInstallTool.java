package com.opower.updater;

import com.opower.updater.admin.LayoutUpdateTable;
import org.apache.hadoop.hbase.HConstants;
import org.junit.Test;
import org.kiji.schema.Kiji;
import org.kiji.schema.KijiNotInstalledException;
import org.kiji.schema.KijiURI;
import org.kiji.schema.tools.BaseTool;

import java.io.IOException;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link com.opower.updater.UpdaterInstallTool}.
 *
 * @author felix.trepanier
 */
public class TestUpdaterInstallTool extends UpdaterToolTest {

    @Test
    public void testCreateInstanceIfItDoesNotExist() throws Exception {
        // check that the instance does not exist
        KijiURI kijiURI = getTestKijiURI();
        try {
            Kiji.Factory.open(kijiURI);
            fail("Instance should not exist at this point.");
        }
        catch (KijiNotInstalledException e) {
            // expected
        }

        assertEquals(BaseTool.SUCCESS, runTool(new UpdaterInstallTool(fakeUpdaterLocker), "--kiji=" + getTestKijiURI()));

        // open the kiji instance, now this should not throw an exception
        Kiji kijiInstance = Kiji.Factory.open(kijiURI);
        assertTrue(kijiInstance.getMetaTable().tableExists(LayoutUpdateTable.TABLE_NAME));
        kijiInstance.release();
    }

    private KijiURI getTestKijiURI() throws IOException {
        String[] zookeepers = getConf().get(HConstants.ZOOKEEPER_QUORUM).split(",");
        return KijiURI.newBuilder()
                .withZookeeperQuorum(zookeepers)
                .withZookeeperClientPort(getKiji().getURI().getZookeeperClientPort())
                .withInstanceName("test").build();
    }
}
