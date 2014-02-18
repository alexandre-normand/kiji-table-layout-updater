package com.opower.updater.operation;

import com.opower.updater.LayoutUpdate;
import com.opower.updater.admin.LayoutUpdateTable;
import com.opower.updater.admin.Update;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.opower.updater.operation.TableCreator}.
 *
 * @author felix.trepanier
 */
public class TestTableCreator extends BaseTableOperatorTest {
    private static final String TABLE_NAME = "table";
    public static final String ORIGINAL_DDL =
            "CREATE TABLE example WITH DESCRIPTION 'Example table.'\n" +
                    "ROW KEY FORMAT RAW\n" +
                    "PROPERTIES (NUMREGIONS = 256)\n" +
                    "WITH LOCALITY GROUP default WITH DESCRIPTION 'Main locality group.' (\n" +
                    "  MAXVERSIONS = INFINITY,\n" +
                    "  TTL = FOREVER,\n" +
                    "  INMEMORY = false,\n" +
                    "  FAMILY familyOne WITH DESCRIPTION 'A family' (\n" +
                    "    someString WITH SCHEMA ID 0\n" +
                    "  )\n" +
                    ");";
    private static final Update INITIAL_UPDATE = new Update(0, ORIGINAL_DDL);

    private TableCreator creator;

    @Before
    @Override
    public void setup() throws IOException {
        super.setup();
        creator = new TableCreator(mockDDLRunner, mockUpdateTable, mockMetaTable);
    }

    @Test
    public void testCanNotCreateATableNamedLayoutUpdate() throws IOException {
        when(mockUpdateTable.getLastUpdateIdForTable(LayoutUpdateTable.TABLE_NAME)).thenReturn(null);
        try {
            creator.createTable(LayoutUpdateTable.TABLE_NAME, new Update(0, "CREATE STATEMENT;"));
            fail();
        }
        catch (OperationForbiddenException e) {
            // ok
        }

        verifyNoDDLNoLayoutUpdate();
    }

    @Test
    public void testCreationFailsIfTableExistsInLayoutUpdate() throws IOException {
        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(12);

        try {
            creator.createTable(TABLE_NAME, INITIAL_UPDATE);
            fail();
        }
        catch (TableAlreadyExistException e) {
            // ok
        }

        verifyNoDDLNoLayoutUpdate();
    }

    @Test
    public void testCreationFailsIfTableExistsInKijiInstance() throws IOException {
        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(null);
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(true);

        try {
            creator.createTable(TABLE_NAME, INITIAL_UPDATE);
        }
        catch (TableAlreadyExistException e) {
            // ok
        }

        verifyNoDDLNoLayoutUpdate();
    }

    @Test
    public void testTableCreation() throws IOException {
        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(null);
        creator.createTable(TABLE_NAME, INITIAL_UPDATE);

        verify(mockDDLRunner).execute(ORIGINAL_DDL);
        verify(mockUpdateTable).insertLayoutUpdate(TABLE_NAME, createLayoutUpdate());
    }

    private LayoutUpdate createLayoutUpdate() {
        return LayoutUpdate.newBuilder()
                .setUpdateId(INITIAL_UPDATE.getId())
                .setAppliedDDL(INITIAL_UPDATE.getDDL())
                .setChecksum("")
                .setValidationFunctionVersion(0)
                .build();
    }
}
