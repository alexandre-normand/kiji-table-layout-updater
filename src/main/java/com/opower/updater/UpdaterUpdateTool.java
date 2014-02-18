package com.opower.updater;

import com.google.common.annotations.VisibleForTesting;
import com.opower.updater.admin.LayoutUpdateTable;
import com.opower.updater.operation.ShellClientTableDDLGenerator;
import com.opower.updater.operation.TableDDLGenerator;
import com.opower.updater.operation.TableUpdater;
import org.kiji.common.flags.Flag;
import org.kiji.schema.tools.KijiToolLauncher;

/**
 * Update Tool. This tool updates the given table by applying all the updates that haven't been applied. Each update
 * is recorded in the table history.
 *
 * @author felix.trepanier
 */
public class UpdaterUpdateTool extends BaseUpdaterTableTool {

    @Flag(name = "table", usage = "URI of the Kiji table to update,"
            + " eg. --table=kiji://hbase-address/kiji-instance/table.")
    private String tableURIFlag = null;

    @Flag(name = "bootstrap", usage = "Use this option when updating a table that was create " +
            "without using the tool. The tool will use the current table layout as the initial version of the table" +
            "and then apply updates.")
    private String bootstrapFlag = null;
    private Boolean bootstrap = false;

    public UpdaterUpdateTool() {
        this(new ZookeeperUpdaterLocker());
    }

    @VisibleForTesting
    UpdaterUpdateTool(UpdaterLocker locker) {
        super(locker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateFlags() throws Exception {
        super.validateFlags();

        if (bootstrapFlag != null && !bootstrapFlag.isEmpty()) {
            bootstrap = Boolean.parseBoolean(bootstrapFlag);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTableURIFlag() {
        return tableURIFlag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int executeTableOperation() throws Exception {
        final TableDDLGenerator tableDDLGenerator = new ShellClientTableDDLGenerator(client);
        final TableUpdater updater = new TableUpdater(ddlRunner, updateTable, kiji.getMetaTable(), tableDDLGenerator);
        getPrintStream().println("Updating table '" + getKijiTableName() + "'.");
        TableUpdater.UpdateResult result = updater.updateTable(getKijiTableName(),
                updateLoader.loadUpdates(getKijiTableName()), bootstrap);
        if (result.wasBootstrapped()) {
            getPrintStream().println("Table '" + result.getTableName() + "' already exists in instance, but it is " +
                    "not defined in " + LayoutUpdateTable.TABLE_NAME + ". Bootstrapping the table in '"
                    + LayoutUpdateTable.TABLE_NAME + "' with the current layout as the initial layout with id 0.");
        }
        getPrintStream().println("Applied " + result.getNumberOfUpdatesApplied()
                + " updates to table '" + result.getTableName() + "'.");
        return SUCCESS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "updater-update";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Apply updates to a given table.";
    }

    /**
     * Program entry point.
     *
     * @param args The command-line arguments.
     * @throws Exception If there is an error.
     */
    public static void main(String[] args) throws Exception {
        System.exit(new KijiToolLauncher().run(new UpdaterUpdateTool(), args));
    }
}
