package com.opower.updater.operation;

import com.opower.updater.LayoutUpdate;
import com.opower.updater.admin.Update;
import com.opower.updater.admin.loader.ResourceUpdateLoader;
import org.junit.Before;
import org.junit.Test;
import org.kiji.schema.shell.DDLException;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.opower.updater.operation.TableUpdater}.
 *
 * @author felix.trepanier
 */
public class TestTableUpdater extends BaseTableOperatorTest {
    private SortedSet<Update> allUpdates;
    private TableUpdater updater;
    private TableDDLGenerator mockTableDDLGenerator;

    @Before
    @Override
    public void setup() throws IOException {
        super.setup();

        allUpdates = ResourceUpdateLoader.DEFAULT.loadUpdates(TABLE_NAME);
        mockTableDDLGenerator = mock(TableDDLGenerator.class);

        updater = new TableUpdater(mockDDLRunner, mockUpdateTable, mockMetaTable, mockTableDDLGenerator);
    }

    @Test
    public void testUpdateFailsIfTheTableDoesNotExistInKijiInstance() throws IOException {
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(false);

        try {
            updater.updateTable(TABLE_NAME, allUpdates, false);
            fail();
        }
        catch (TableDoesNotExistException e) {
            // ok
        }

        verifyNoDDLNoLayoutUpdate();
    }

    @Test
    public void testUpdateFailsIfTheTableDoesNotExistInLayoutUpdateIfBootstrapIsFalse() throws IOException {
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(false);
        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(null);

        try {
            updater.updateTable(TABLE_NAME, allUpdates, false);
            fail();
        }
        catch (TableDoesNotExistException e) {
            // ok
        }

        verifyNoDDLNoLayoutUpdate();
    }

    @Test
    public void testAllUpdatesApplied() throws IOException {
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(true);
        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(0);

        updater.updateTable(TABLE_NAME, allUpdates, false);

        verify(mockDDLRunner).execute(getFirstUpdate().getDDL());
        verify(mockDDLRunner).execute(getSecondUpdate().getDDL());
    }

    @Test
    public void testUpdateResult() throws IOException {
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(true);
        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(0);

        TableUpdater.UpdateResult result = updater.updateTable(TABLE_NAME, allUpdates, false);
        assertEquals(TABLE_NAME, result.getTableName());
        assertEquals(new Integer(2), result.getNumberOfUpdatesApplied());
        assertFalse(result.wasBootstrapped());
    }

    @Test
    public void testUpdatesStartAfterLastKnownId() throws IOException {
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(true);
        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(1);

        updater.updateTable(TABLE_NAME, allUpdates, false);

        verify(mockDDLRunner).execute(allUpdates.last().getDDL());
        verifyNoMoreInteractions(mockDDLRunner);
    }

    @Test
    public void testLayoutUpdateTableIsUpdated() throws IOException {
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(true);
        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(0);

        updater.updateTable(TABLE_NAME, allUpdates, false);

        LayoutUpdate firstLayoutUpdate = LayoutUpdate.newBuilder()
                .setUpdateId(1)
                .setChecksum("")
                .setValidationFunctionVersion(0)
                .setAppliedDDL(getFirstUpdate().getDDL()).build();

        LayoutUpdate secondLayoutUpdate = LayoutUpdate.newBuilder()
                .setUpdateId(2)
                .setChecksum("")
                .setValidationFunctionVersion(0)
                .setAppliedDDL(getSecondUpdate().getDDL()).build();

        verify(mockUpdateTable).insertLayoutUpdate(TABLE_NAME, firstLayoutUpdate);
        verify(mockUpdateTable).insertLayoutUpdate(TABLE_NAME, secondLayoutUpdate);
    }

    @Test
    public void testMissingUpdateIdFails() throws IOException {
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(true);

        allUpdates.add(new Update(88, "DUMMY"));

        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(1);

        try {
            updater.updateTable(TABLE_NAME, allUpdates, false);
            fail();
        }
        catch (MissingUpdateException e) {
            // ok
        }

        ArgumentCaptor<LayoutUpdate> updateCaptor = ArgumentCaptor.forClass(LayoutUpdate.class);
        verify(mockUpdateTable, times(1)).insertLayoutUpdate(eq(TABLE_NAME), updateCaptor.capture());

        assertEquals(new Integer(2), updateCaptor.getValue().getUpdateId());
    }

    @Test
    public void testMissingUpdateOneFails() throws IOException {
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(true);

        SortedSet<Update> updates = new TreeSet<Update>(Update.UPDATE_COMPARATOR);
        updates.add(new Update(88, "DUMMY"));

        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(0);

        try {
            updater.updateTable(TABLE_NAME, updates, false);
            fail();
        }
        catch (MissingUpdateException e) {
            // ok
        }

        verifyNoDDLNoLayoutUpdate();
    }

    @Test
    public void testUpdateProcessAbortOnDDLExecutionFailure() throws IOException {
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(true);
        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(1);
        doThrow(new DDLException("Bad ORIGINAL_DDL Statement")).when(mockDDLRunner).execute(anyString());

        Update badUpdate = new Update(2, "ALTER TABLE test ADD COLUMN wrong:wrong WITH SCHEMA ID 0;");

        TreeSet<Update> updates = new TreeSet<Update>(Update.UPDATE_COMPARATOR);
        updates.add(badUpdate);

        try {
            updater.updateTable(TABLE_NAME, updates, false);
            fail();
        }
        catch (UpdateException e) {
            assertEquals(2, e.getFailedUpdate().getId());
        }
        verify(mockUpdateTable, never()).insertLayoutUpdate(anyString(), (LayoutUpdate) anyObject());
    }

    @Test
    public void supportTheUpdateOfAnExistingTableCreatedPriorToTheUseOfTheUpdaterTool() throws IOException {
        String createDDL = "CREATE DDL STATEMENT;";
        when(mockMetaTable.tableExists(TABLE_NAME)).thenReturn(true);
        when(mockUpdateTable.getLastUpdateIdForTable(TABLE_NAME)).thenReturn(null, 0);
        when(mockTableDDLGenerator.generateTableLayoutDDL(TABLE_NAME)).thenReturn(createDDL);

        TableUpdater.UpdateResult result = updater.updateTable(TABLE_NAME, allUpdates, true);
        assertTrue(result.wasBootstrapped());

        ArgumentCaptor<LayoutUpdate> layoutUpdateCaptor = ArgumentCaptor.forClass(LayoutUpdate.class);
        verify(mockUpdateTable, times(3)).insertLayoutUpdate(Matchers.eq(TABLE_NAME), layoutUpdateCaptor.capture());

        LayoutUpdate initialLayoutUpdate = layoutUpdateCaptor.getAllValues().get(0);
        assertEquals(new Integer(0), initialLayoutUpdate.getUpdateId());
        assertEquals(createDDL, initialLayoutUpdate.getAppliedDDL());

        LayoutUpdate firstLayoutUpdate = LayoutUpdate.newBuilder()
                .setUpdateId(getFirstUpdate().getId())
                .setChecksum("")
                .setValidationFunctionVersion(0)
                .setAppliedDDL(getFirstUpdate().getDDL()).build();

        LayoutUpdate secondLayoutUpdate = LayoutUpdate.newBuilder()
                .setUpdateId(getSecondUpdate().getId())
                .setChecksum("")
                .setValidationFunctionVersion(0)
                .setAppliedDDL(getSecondUpdate().getDDL()).build();

        assertEquals(firstLayoutUpdate, layoutUpdateCaptor.getAllValues().get(1));
        assertEquals(secondLayoutUpdate, layoutUpdateCaptor.getAllValues().get(2));
    }

    private Update getFirstUpdate() {
        return allUpdates.tailSet(Update.fromUpdate(1)).first();
    }

    private Update getSecondUpdate() {
        return allUpdates.tailSet(Update.fromUpdate(2)).first();
    }
}
