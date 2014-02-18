package com.opower.updater.admin;

import com.opower.updater.LayoutUpdate;
import org.junit.Before;
import org.junit.Test;
import org.kiji.schema.Kiji;
import org.kiji.schema.KijiClientTest;
import org.kiji.schema.KijiDataRequest;
import org.kiji.schema.KijiRowData;
import org.kiji.schema.KijiTable;
import org.kiji.schema.KijiTableReader;

import java.io.IOException;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * Test class for {@link com.opower.updater.admin.KijiLayoutUpdateTable}.
 *
 * @author felix.trepanier
 */
public class TestKijiLayoutUpdateTable extends KijiClientTest {
    private static final String FUNKY_TABLE = "funky";

    private Kiji kiji;
    private KijiLayoutUpdateTable layoutUpdateTable;

    @Before
    public void setup() throws IOException {
        kiji = getKiji();

        KijiLayoutUpdateTableUtils.ensureLayoutUpdateTableExists(kiji);
        layoutUpdateTable = KijiLayoutUpdateTable.newInstance(kiji);
    }

    @Test
    public void testGetLatestUpdateIdIfTableEmpty() throws IOException {
        assertNull(layoutUpdateTable.getLastUpdateIdForTable("unknown"));
    }

    @Test
    public void testLayoutUpdatesAreInsertedAndRetrieved() throws IOException {
        LayoutUpdate update = createLayoutUpdate(0);

        layoutUpdateTable.insertLayoutUpdate(FUNKY_TABLE, update);

        LayoutUpdate retrievedUpdate = getLatestUpdate(FUNKY_TABLE);
        assertEquals(update, retrievedUpdate);
    }

    @Test
    public void testGetLatestUpdateId() throws IOException {
        LayoutUpdate oldUpdate = createLayoutUpdate(0);
        LayoutUpdate newUpdate = createLayoutUpdate(1);

        layoutUpdateTable.insertLayoutUpdate(FUNKY_TABLE, oldUpdate);
        layoutUpdateTable.insertLayoutUpdate(FUNKY_TABLE, newUpdate);

        assertEquals(newUpdate.getUpdateId(), layoutUpdateTable.getLastUpdateIdForTable(FUNKY_TABLE));
    }

    @Test
    public void testGetAllTableHistory() throws IOException, InterruptedException {
        LayoutUpdate oldUpdate = createLayoutUpdate(0);
        LayoutUpdate newUpdate = createLayoutUpdate(1);

        insertTwoLayoutUpdates(oldUpdate, newUpdate);

        Collection<LayoutUpdate> history = layoutUpdateTable.getTableHistory(FUNKY_TABLE, null);
        assertEquals(2, history.size());
        assertEquals(newUpdate, history.iterator().next());
        assertTrue(history.contains(newUpdate));
    }

    @Test
    public void testGetRestrictedTableHistory() throws IOException, InterruptedException {
        LayoutUpdate oldUpdate = createLayoutUpdate(0);
        LayoutUpdate newUpdate = createLayoutUpdate(1);

        insertTwoLayoutUpdates(oldUpdate, newUpdate);

        Collection<LayoutUpdate> history = layoutUpdateTable.getTableHistory(FUNKY_TABLE, 1);
        assertEquals(1, history.size());
        assertEquals(newUpdate, history.iterator().next());
    }

    private LayoutUpdate createLayoutUpdate(Integer id) {
        return LayoutUpdate.newBuilder()
                .setUpdateId(id)
                .setChecksum("check")
                .setValidationFunctionVersion(0)
                .setAppliedDDL("ORIGINAL_DDL")
                .build();
    }

    private void insertTwoLayoutUpdates(LayoutUpdate oldUpdate, LayoutUpdate newUpdate)
            throws IOException, InterruptedException {
        layoutUpdateTable.insertLayoutUpdate(FUNKY_TABLE, oldUpdate);
        // Needed otherwise the test is unstable if the two writes happens at the same hbase timestamp
        // in which case the second insertion overrides the first and there is only a single update in the store.
        Thread.sleep(10);
        layoutUpdateTable.insertLayoutUpdate(FUNKY_TABLE, newUpdate);
    }

    private LayoutUpdate getLatestUpdate(String tableName) throws IOException {
        KijiTable table = kiji.openTable(LayoutUpdateTable.TABLE_NAME);
        KijiTableReader reader = table.openTableReader();

        KijiRowData data = reader.get(table.getEntityId(tableName),
                KijiDataRequest.create(LayoutUpdateTable.UPDATE_LOG_FAMILY_NAME, LayoutUpdateTable.LAYOUT_UPDATE_COLUMN_NAME));
        return data.getMostRecentValue(LayoutUpdateTable.UPDATE_LOG_FAMILY_NAME, LayoutUpdateTable.LAYOUT_UPDATE_COLUMN_NAME);
    }
}
