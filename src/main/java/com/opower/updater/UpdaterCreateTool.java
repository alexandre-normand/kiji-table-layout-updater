package com.opower.updater;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.opower.updater.admin.loader.DDLTokenReplacer;
import com.opower.updater.admin.loader.ResourceUpdateLoader;
import com.opower.updater.admin.loader.UpdateLoader;
import com.opower.updater.admin.loader.UpdateLoaderWithPreProcessor;
import com.opower.updater.operation.ShellClientTableDDLGenerator;
import com.opower.updater.operation.TableCreator;
import com.opower.updater.operation.TableDDLGenerator;
import com.opower.updater.operation.TableUpdater;
import org.kiji.common.flags.Flag;
import org.kiji.schema.tools.KijiToolLauncher;

import java.util.HashMap;
import java.util.Map;

/**
 * Create Tool. This tool creates new kiji tables and apply all the currently known updates.
 * The number of regions to use when creating the table can be specified at the command line using the
 * 'num-regions' parameter. This tool inserts update information in the layout_update table to keep the table
 * history and to know which updates have been applied on the table.
 *
 * @author felix.trepanier
 */
public class UpdaterCreateTool extends BaseUpdaterTableTool {
    public static final String NUM_REGIONS_TOKEN = "num_regions";

    @Flag(name = "table", usage = "URI of the Kiji table to create," +
            " eg. --table=kiji://hbase-address/kiji-instance/table.")
    private String tableURIFlag = null;

    @Flag(name = "num-regions",
            usage = "Number (>= 1) of initial regions to create in the table. " +
                    "Regions are evenly sized across the HBase row key space.\n")
    private String mNumRegionsFlag = null;
    private int mNumRegions = 1;

    public UpdaterCreateTool() {
        this(new ZookeeperUpdaterLocker());
    }

    @VisibleForTesting
    UpdaterCreateTool(UpdaterLocker locker) {
        super(locker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateFlags() throws Exception {
        super.validateFlags();

        if ((mNumRegionsFlag != null) && !mNumRegionsFlag.isEmpty()) {
            mNumRegions = Integer.parseInt(mNumRegionsFlag);
            Preconditions.checkArgument(mNumRegions >= 1,
                    "Invalid initial number of regions {}, must be >= 1.", mNumRegions);
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
        final TableCreator creator = new TableCreator(ddlRunner, updateTable, kiji.getMetaTable());
        final TableDDLGenerator tableDDLGenerator = new ShellClientTableDDLGenerator(client);
        final TableUpdater updater = new TableUpdater(ddlRunner, updateTable, kiji.getMetaTable(), tableDDLGenerator);

        getPrintStream().println("Creating table " + getKijiTableName());
        creator.createTable(getKijiTableName(), updateLoader.loadCreateTable(getKijiTableName()));

        getPrintStream().println("Updating table " + getKijiTableName());
        updater.updateTable(getKijiTableName(), updateLoader.loadUpdates(getKijiTableName()), false);

        return SUCCESS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UpdateLoader createUpdateLoader() {
        Map<String, String> tokenMap = new HashMap<String, String>(1);
        tokenMap.put(NUM_REGIONS_TOKEN, Integer.toString(mNumRegions));
        return new UpdateLoaderWithPreProcessor(ResourceUpdateLoader.DEFAULT, new DDLTokenReplacer(tokenMap));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "updater-create";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Create a table and apply updates.";
    }

    /**
     * Program entry point.
     *
     * @param args The command-line arguments.
     * @throws Exception If there is an error.
     */
    public static void main(String[] args) throws Exception {
        System.exit(new KijiToolLauncher().run(new UpdaterCreateTool(), args));
    }
}
