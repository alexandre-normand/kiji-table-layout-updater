package com.opower.updater.operation;

import com.opower.updater.LayoutUpdate;
import com.opower.updater.admin.LayoutUpdateTable;
import com.opower.updater.admin.Update;
import com.opower.updater.ddl.DDLRunner;
import org.kiji.schema.KijiMetaTable;

import java.io.IOException;

/**
 * Base class for table operators.
 *
 * @author felix.trepanier
 */
public abstract class TableOperator {
    protected final DDLRunner ddlRunner;
    protected final LayoutUpdateTable layoutUpdateTable;
    protected final KijiMetaTable metaTable;

    /**
     * Base constructor for {@link com.opower.updater.operation.TableOperator} classes.
     *
     * @param ddlRunner         The DDLRunner used to execute DDL statements.
     * @param layoutUpdateTable The layout_update table.
     * @param metaTable         Kiji MetaTable
     */
    protected TableOperator(DDLRunner ddlRunner,
                            LayoutUpdateTable layoutUpdateTable,
                            KijiMetaTable metaTable) {
        this.ddlRunner = ddlRunner;
        this.layoutUpdateTable = layoutUpdateTable;
        this.metaTable = metaTable;
    }

    /**
     * Apply a single update.
     *
     * @param update The update to apply.
     * @throws java.io.IOException
     */
    protected LayoutUpdate applyUpdate(Update update) throws IOException {
        ddlRunner.execute(update.getDDL());
        return LayoutUpdate.newBuilder()
                .setUpdateId(update.getId())
                .setChecksum("") //TODO to implement
                .setValidationFunctionVersion(0) //TODO to implement
                .setAppliedDDL(update.getDDL())
                .build();
    }

    /**
     * Check that the table exists both in kiji meta table and in the layout_update table.
     *
     * @param tableName The table name.
     * @throws IOException
     */
    protected void checkTableExists(String tableName) throws IOException {
        checkTableExistsInInstance(tableName);
        checkTableExistsInLayoutUpdateTable(tableName);
    }

    /**
     * Check that the table exists in the layout_update table.
     *
     * @param tableName The table name.
     * @throws IOException
     */
    protected void checkTableExistsInLayoutUpdateTable(String tableName) throws IOException {
        if (layoutUpdateTable.getLastUpdateIdForTable(tableName) == null) {
            throw new TableDoesNotExistException(tableName, "Table does not exist in " + LayoutUpdateTable.TABLE_NAME);
        }
    }

    /**
     * Check that the table exists in the kiji instance.
     *
     * @param tableName The table name.
     * @throws IOException
     */
    protected void checkTableExistsInInstance(String tableName) throws IOException {
        if (!metaTable.tableExists(tableName)) {
            throw new TableDoesNotExistException(tableName, "Table does not exist in Kiji instance.");
        }
    }

    /**
     * Check that the table to be operated on is not the layout_update table.
     *
     * @param tableName The table name.
     */
    protected void checkTableIsNotLayoutUpdateTable(String tableName) {
        if (tableName.equalsIgnoreCase(LayoutUpdateTable.TABLE_NAME)) {
            throw new OperationForbiddenException("Can not execute operation on admin table '" +
                    LayoutUpdateTable.TABLE_NAME + "'");
        }
    }
}
