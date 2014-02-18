package com.opower.updater.operation;

import com.opower.updater.LayoutUpdate;
import com.opower.updater.admin.LayoutUpdateTable;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.opower.updater.operation.TableDeleter}.
 *
 * @author felix.trepanier
 */
public class TestTableDeleter extends BaseTableOperatorTest {

    private TableDeleter deleter;

    @Before
    @Override
    public void setup() throws IOException {
        super.setup();
        deleter = new TableDeleter(mockDDLRunner, mockUpdateTable, mockMetaTable);
    }

    @Test
    public void testCanNotDeleteLayoutUpdateTable() throws IOException {
        when(mockMetaTable.tableExists(LayoutUpdateTable.TABLE_NAME)).thenReturn(true);
        when(mockUpdateTable.getLastUpdateIdForTable(LayoutUpdateTable.TABLE_NAME)).thenReturn(12);

        try {
            deleter.dropTable(LayoutUpdateTable.TABLE_NAME);
            fail();
        }
        catch (OperationForbiddenException e) {
            // ok
        }

        verifyNoDDLNoLayoutUpdate();
    }

    @Test
    public void testDeleteDoesNothingIfTableDoesNotExistsInKijiInstance() throws IOException {
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(false);

        try {
            deleter.dropTable(TABLE_NAME);
            fail();
        }
        catch (TableDoesNotExistException e) {
            // ok
        }

        verifyNoDDLNoLayoutUpdate();
    }

    @Test
    public void testDeleteDoesNothingIfTableDoesNotExistsInLayoutUpdate() throws IOException {
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(true);
        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(null);

        try {
            deleter.dropTable(TABLE_NAME);
            fail();
        }
        catch (TableDoesNotExistException e) {
            // ok
        }

        verifyNoDDLNoLayoutUpdate();
    }

    @Test
    public void testDropTable() throws IOException {
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(true);
        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(44);

        deleter.dropTable(TABLE_NAME);

        String ddl = "DROP TABLE " + TABLE_NAME + ";";
        verify(mockDDLRunner).execute(ddl);
        verify(mockUpdateTable).insertLayoutUpdate(TABLE_NAME, LayoutUpdate.newBuilder()
                .setUpdateId(null)
                .setAppliedDDL(ddl)
                .setChecksum("")
                .setValidationFunctionVersion(0)
                .build());
    }
}
