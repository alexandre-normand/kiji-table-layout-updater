package com.opower.updater.operation;

import com.opower.updater.LayoutUpdate;
import com.opower.updater.admin.LayoutUpdateTable;
import com.opower.updater.ddl.DDLRunner;
import org.kiji.schema.KijiMetaTable;

import java.io.IOException;

/**
 * Class responsible for dropping an existing table.
 *
 * @author felix.trepanier
 */
public class TableDeleter extends TableOperator {
    private static final String DROP_TABLE_DDL = "DROP TABLE %s;";

    /**
     * Constructor the {@link com.opower.updater.operation.TableDeleter}.
     *
     * @param ddlRunner         The DDLRunner used to execute DDL statements.
     * @param layoutUpdateTable The layout_update table.
     * @param metaTable         Kiji MetaTable
     */
    public TableDeleter(DDLRunner ddlRunner,
                        LayoutUpdateTable layoutUpdateTable,
                        KijiMetaTable metaTable) {
        super(ddlRunner, layoutUpdateTable, metaTable);
    }

    /**
     * Drop an existing table.
     *
     * @param tableName The name of the table to drop.
     * @throws IOException
     */
    public void dropTable(String tableName) throws IOException {
        checkTableExists(tableName);
        checkTableIsNotLayoutUpdateTable(tableName);

        String dropTableDDL = String.format(DROP_TABLE_DDL, tableName);
        ddlRunner.execute(dropTableDDL);
        layoutUpdateTable.insertLayoutUpdate(tableName, createDeleteLayoutUpdate(dropTableDDL));
    }

    private LayoutUpdate createDeleteLayoutUpdate(String dropTableStatement) {
        return LayoutUpdate.newBuilder()
                .setUpdateId(null)
                .setAppliedDDL(dropTableStatement)
                .setChecksum("")
                .setValidationFunctionVersion(0)
                .build();
    }
}
