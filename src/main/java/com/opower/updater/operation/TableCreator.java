package com.opower.updater.operation;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.opower.updater.LayoutUpdate;
import com.opower.updater.admin.LayoutUpdateTable;
import com.opower.updater.admin.Update;
import com.opower.updater.ddl.DDLRunner;
import org.kiji.schema.KijiMetaTable;

import java.io.IOException;

/**
 * Class responsible for creating a new table.
 *
 * @author felix.trepanier
 */
public class TableCreator extends TableOperator {

    /**
     * Constructor the {@link com.opower.updater.operation.TableCreator}.
     *
     * @param ddlRunner         The DDLRunner used to execute DDL statements.
     * @param layoutUpdateTable The layout_update table.
     * @param metaTable         Kiji MetaTable
     */
    public TableCreator(DDLRunner ddlRunner,
                        LayoutUpdateTable layoutUpdateTable,
                        KijiMetaTable metaTable) {
        super(ddlRunner, layoutUpdateTable, metaTable);
    }

    /**
     * Create the table.
     *
     * @param tableName     The name of the table to create.
     * @param initialUpdate The update DDL that creates the table.
     * @throws IOException
     */
    public void createTable(String tableName, Update initialUpdate) throws IOException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(tableName));
        checkTableIsNotLayoutUpdateTable(tableName);
        Preconditions.checkArgument(initialUpdate.getId() == 0);

        Integer lastUpdateId = layoutUpdateTable.getLastUpdateIdForTable(tableName);
        if (lastUpdateId != null) {
            throw new TableAlreadyExistException(tableName, "Table " + tableName + " already exists.");
        }

        if (metaTable.tableExists(tableName)) {
            throw new TableAlreadyExistException(tableName,
                    "Table " + tableName + "already exists in the instance but not in " + LayoutUpdateTable.TABLE_NAME);
        }

        LayoutUpdate layoutUpdate = applyUpdate(initialUpdate);
        layoutUpdateTable.insertLayoutUpdate(tableName, layoutUpdate);
    }
}
