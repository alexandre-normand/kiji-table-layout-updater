package com.opower.updater.operation;

import com.opower.updater.LayoutUpdate;
import com.opower.updater.admin.LayoutUpdateTable;
import com.opower.updater.ddl.DDLRunner;
import org.kiji.schema.KijiMetaTable;

import java.io.IOException;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Base test class to provide setup for mocks and other test utility methods for
 * {@link com.opower.updater.operation.TableOperator} test classes.
 *
 * @author felix.trepanier
 */
public class BaseTableOperatorTest {
    protected static final String TABLE_NAME = "test";

    protected DDLRunner mockDDLRunner;
    protected LayoutUpdateTable mockUpdateTable;
    protected KijiMetaTable mockMetaTable;

    protected void setup() throws IOException {
        mockDDLRunner = mock(DDLRunner.class);
        mockUpdateTable = mock(LayoutUpdateTable.class);
        mockMetaTable = mock(KijiMetaTable.class);
    }

    /**
     * Verify that there are no DDL statement executed nor any update in the layout_update table.
     *
     * @throws IOException
     */
    protected void verifyNoDDLNoLayoutUpdate() throws IOException {
        verifyZeroInteractions(mockDDLRunner);
        verify(mockUpdateTable, never()).insertLayoutUpdate(anyString(), (LayoutUpdate) anyObject());
    }
}
