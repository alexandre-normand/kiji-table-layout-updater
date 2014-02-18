package com.opower.updater;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import org.kiji.common.flags.Flag;
import org.kiji.schema.tools.KijiToolLauncher;

/**
 * History Tool. This tool displays the table update history. The number of updates to display can be set using the
 * 'num-versions' parameter.
 *
 * @author felix.trepanier
 */
public class UpdaterHistoryTool extends BaseUpdaterTableTool {

    @Flag(name = "table", usage = "URI of the Kiji table to show the history,"
            + " eg. --table=kiji://hbase-address/kiji-instance/table.")
    private String tableURIFlag = null;

    @Flag(name = "num-versions", usage = "The maximum number of versions to show in the table history.")
    private String numVersionsFlag = null;
    private Integer numVersions = null;

    public UpdaterHistoryTool() {
        this(new ZookeeperUpdaterLocker());
    }

    @VisibleForTesting
    UpdaterHistoryTool(UpdaterLocker locker) {
        super(locker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateFlags() throws Exception {
        super.validateFlags();

        if ((numVersionsFlag != null) && !numVersionsFlag.isEmpty()) {
            numVersions = Integer.parseInt(numVersionsFlag);
            Preconditions.checkArgument(numVersions >= 1,
                    "Invalid number of versions {}, must be >= 1.", numVersions);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int executeTableOperation() throws Exception {
        getPrintStream().println("History for table: " + getKijiTableName());
        for (LayoutUpdate layoutUpdate : updateTable.getTableHistory(getKijiTableName(), numVersions)) {
            getPrintStream().println(layoutUpdate.toString());
        }
        return SUCCESS;
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
    public String getName() {
        return "updater-history";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Get the update history for a given table.";
    }

    /**
     * Program entry point.
     *
     * @param args The command-line arguments.
     * @throws Exception If there is an error.
     */
    public static void main(String[] args) throws Exception {
        System.exit(new KijiToolLauncher().run(new UpdaterHistoryTool(), args));
    }
}
