package com.opower.updater.operation;

import com.opower.updater.LayoutUpdate;
import com.opower.updater.admin.LayoutUpdateTable;
import com.opower.updater.admin.Update;
import com.opower.updater.ddl.DDLRunner;
import org.kiji.schema.KijiMetaTable;

import java.io.IOException;
import java.util.SortedSet;

/**
 * Class responsible for updating a table by sequentially applying the updates.
 *
 * @author felix.trepanier
 */
public class TableUpdater extends TableOperator {

    private final TableDDLGenerator tableDDLGenerator;

    /**
     * Constructor the {@link com.opower.updater.operation.TableUpdater}.
     *
     * @param ddlRunner         The DDLRunner used to execute DDL statements.
     * @param layoutUpdateTable The layout_update table.
     * @param metaTable         Kiji MetaTable
     */
    public TableUpdater(DDLRunner ddlRunner,
                        LayoutUpdateTable layoutUpdateTable,
                        KijiMetaTable metaTable,
                        TableDDLGenerator tableDDLGenerator) {
        super(ddlRunner, layoutUpdateTable, metaTable);
        this.tableDDLGenerator = tableDDLGenerator;
    }

    /**
     * Update a given table.
     *
     * @param tableName The name of the table to load updates for.
     * @param updates   The sorted set of updates.
     * @param bootstrap If the table exists in the instance but not in layout_update, bootstrap it in layout_update.
     * @throws IOException
     */
    public UpdateResult updateTable(String tableName, SortedSet<Update> updates, boolean bootstrap) throws IOException {
        boolean wasBootstrapped = false;
        checkTableExistsInInstance(tableName);
        if (!bootstrap) {
            // If we are not bootstrapping an existing instance with the updater tool, then the table needs to
            // exist in the layout_update table.
            checkTableExistsInLayoutUpdateTable(tableName);
        }
        else if (layoutUpdateTable.getLastUpdateIdForTable(tableName) == null) {
            // If we are bootstrapping an existing instance to use the updater tool and the table is not found
            // in the layout_update table, then insert the initial update based on the table current layout.
            bootstrapExistingTableInLayoutUpdateTable(tableName);
            wasBootstrapped = true;
        }

        Integer id = layoutUpdateTable.getLastUpdateIdForTable(tableName);
        Integer nextUpdate = id + 1;
        SortedSet<Update> updatesToApply = updates.tailSet(Update.fromUpdate(nextUpdate));

        Integer expectedUpdateId = nextUpdate;
        for (Update update : updatesToApply) {
            if (update.getId() != expectedUpdateId) {
                throw new MissingUpdateException(expectedUpdateId, update.getId());
            }

            try {
                LayoutUpdate layoutUpdate = applyUpdate(update);
                layoutUpdateTable.insertLayoutUpdate(tableName, layoutUpdate);
                expectedUpdateId += 1;
            }
            catch (Exception ex) {
                throw new UpdateException(tableName, update, ex);
            }
        }

        return new UpdateResult(tableName, wasBootstrapped, updatesToApply.size(), updates.last().getId());
    }

    private void bootstrapExistingTableInLayoutUpdateTable(String tableName) throws IOException {
        String currentTableDDL = tableDDLGenerator.generateTableLayoutDDL(tableName);
        LayoutUpdate initialLayoutUpdate = LayoutUpdate.newBuilder()
                .setUpdateId(0)
                .setAppliedDDL(currentTableDDL)
                .setChecksum("")  // TODO check with the expected checksum from the create DDL
                .setValidationFunctionVersion(0)
                .build();
        layoutUpdateTable.insertLayoutUpdate(tableName, initialLayoutUpdate);
    }

    /**
     * Class representing the update result.
     */
    public static class UpdateResult {
        private final String tableName;
        private final boolean wasBootstrapped;
        private final Integer numberOfUpdatesApplied;
        private final Integer idOfTheLastUpdateApplied;

        public UpdateResult(String tableName, boolean wasBootstrapped, Integer numberOfUpdatesApplied,
                            Integer idOfTheLastUpdateApplied) {
            this.tableName = tableName;
            this.wasBootstrapped = wasBootstrapped;
            this.numberOfUpdatesApplied = numberOfUpdatesApplied;
            this.idOfTheLastUpdateApplied = idOfTheLastUpdateApplied;
        }

        /**
         * @return The updated table name.
         */
        public String getTableName() {
            return tableName;
        }

        /**
         * @return true if the table was bootstrapped in the layout_update table, false otherwise
         */
        public boolean wasBootstrapped() {
            return wasBootstrapped;
        }

        /**
         * @return The number of applied updates.
         */
        public Integer getNumberOfUpdatesApplied() {
            return numberOfUpdatesApplied;
        }

        public Integer getIdOfTheLastUpdateApplied() {
            return idOfTheLastUpdateApplied;
        }
    }
}
