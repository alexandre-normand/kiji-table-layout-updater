package com.opower.updater;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
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
import org.kiji.schema.avro.CompressionType;
import org.kiji.schema.shell.ddl.CompressionTypeToken;
import org.kiji.schema.tools.KijiToolLauncher;
import scala.Enumeration;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import static java.lang.String.format;

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
    public static final String COMPRESSION_TOKEN = "compression";

    @Flag(name = "table", usage = "URI of the Kiji table to create," +
            " eg. --table=kiji://hbase-address/kiji-instance/table.")
    private String tableURIFlag = null;

    @Flag(name = "num-regions",
            usage = "Number (>= 1) of initial regions to create in the table. " +
                    "Regions are evenly sized across the HBase row key space.\n")
    private String mNumRegionsFlag = null;
    private int mNumRegions = 1;

    @Flag(name = "compression",
            usage = "Compression type to use to create the table (one of NONE, LZO, GZ, SNAPPY). " +
                    "Only effective if the table creation DDL contains a \"COMPRESSED WITH %%%compression%%%\" line. " +
                    "Defaults to NONE.\n")
    private String mCompressionFlag = null;
    private Enumeration.Value mCompression = CompressionTypeToken.NONE();

    @Flag(name = "set-layout-id", usage = "Use this option to have the tool set the table layout-id to the last" +
            "update id or not after a successful kiji-table-updater run. The default is 'true'.")
    private String setLayoutIdFlag = null;
    private Boolean setLayoutId = true;

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

        if ((mCompressionFlag != null) && !mCompressionFlag.isEmpty()) {
            try {
                mCompression = CompressionTypeToken.withName(mCompressionFlag.toUpperCase());
            }
            catch(NoSuchElementException e) {
                throw new IllegalArgumentException(format("Invalid compression type [%s], must be one of [%s]",
                        mCompressionFlag, Joiner.on(", ").join(CompressionType.values())));
            }
        }

        if (setLayoutIdFlag != null && !setLayoutIdFlag.isEmpty()) {
            setLayoutId = Boolean.parseBoolean(setLayoutIdFlag);
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
        TableUpdater.UpdateResult updateResult =
                updater.updateTable(getKijiTableName(), updateLoader.loadUpdates(getKijiTableName()), false);

        if(setLayoutId) {
            setLayoutIdAfterUpdate(updateResult);
        }

        return SUCCESS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UpdateLoader createUpdateLoader() {
        Map<String, String> tokenMap = new HashMap<String, String>(1);
        tokenMap.put(NUM_REGIONS_TOKEN, Integer.toString(mNumRegions));
        tokenMap.put(COMPRESSION_TOKEN, mCompression.toString());
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
