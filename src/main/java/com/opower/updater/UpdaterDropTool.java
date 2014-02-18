package com.opower.updater;

import com.google.common.annotations.VisibleForTesting;
import com.opower.updater.operation.TableDeleter;
import org.kiji.common.flags.Flag;
import org.kiji.schema.tools.KijiToolLauncher;

/**
 * Drop tool. This tool drops tables and insert a record in the table history.
 *
 * @author felix.trepanier
 */
public class UpdaterDropTool extends BaseUpdaterTableTool {
    @Flag(name = "table", usage = "URI of the Kiji table to drop, " +
            "eg. --table=kiji://hbase-address/kiji-instance/table.")
    private String tableURIFlag = null;

    public UpdaterDropTool() {
        this(new ZookeeperUpdaterLocker());
    }

    @VisibleForTesting
    UpdaterDropTool(UpdaterLocker locker) {
        super(locker);
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
        if (isInteractive() && !inputConfirmation(
                String.format("Are you sure you want to delete Kiji table '%s'?", getKijiTableName()),
                getKijiTableName())) {
            getPrintStream().println("Delete aborted.");
            return FAILURE;
        }
        final TableDeleter deleter = new TableDeleter(ddlRunner, updateTable, kiji.getMetaTable());

        getPrintStream().println("Dropping table " + getKijiTableName());
        deleter.dropTable(getKijiTableName());

        return SUCCESS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "updater-drop";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Drop a table.";
    }

    /**
     * Program entry point.
     *
     * @param args The command-line arguments.
     * @throws Exception If there is an error.
     */
    public static void main(String[] args) throws Exception {
        System.exit(new KijiToolLauncher().run(new UpdaterDropTool(), args));
    }
}
