package com.opower.updater.admin;

import org.junit.Before;
import org.junit.Test;
import org.kiji.schema.Kiji;
import org.kiji.schema.KijiClientTest;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Test class for {@link KijiLayoutUpdateTableUtils}.
 *
 * @author felix.trepanier
 */
public class TestKijiLayoutUpdateTableUtils extends KijiClientTest {
    private Kiji kiji;

    @Before
    public void setup() throws IOException {
        kiji = getKiji();
    }

    @Test
    public void shouldCreateTheLayoutUpdateTableIfItDoesNotExist() throws IOException {
        boolean wasCreated = KijiLayoutUpdateTableUtils.ensureLayoutUpdateTableExists(kiji);

        assertTrue(wasCreated);

        List<String> afterTableNames = kiji.getTableNames();
        assertTrue(afterTableNames.contains(LayoutUpdateTable.TABLE_NAME));
    }

    @Test
    public void shouldInsertAnUpdateInTheLayoutUpdateTableAfterCreation() throws IOException {
        boolean wasCreated = KijiLayoutUpdateTableUtils.ensureLayoutUpdateTableExists(kiji);
        assertTrue(wasCreated);

        KijiLayoutUpdateTable layoutUpdateTable = KijiLayoutUpdateTable.newInstance(kiji);
        assertEquals(new Integer(0), layoutUpdateTable.getLastUpdateIdForTable(LayoutUpdateTable.TABLE_NAME));
    }

    @Test
    public void shouldNotCreateTheLayoutUpdateTableIfItExists() throws IOException {
        KijiLayoutUpdateTableUtils.ensureLayoutUpdateTableExists(kiji);

        boolean wasCreated = KijiLayoutUpdateTableUtils.ensureLayoutUpdateTableExists(kiji);
        assertFalse(wasCreated);
    }
}
