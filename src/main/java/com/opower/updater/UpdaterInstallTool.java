package com.opower.updater;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.opower.updater.admin.KijiLayoutUpdateTableUtils;
import com.opower.updater.admin.LayoutUpdateTable;
import org.apache.hadoop.conf.Configuration;
import org.kiji.common.flags.Flag;
import org.kiji.schema.Kiji;
import org.kiji.schema.KijiInstaller;
import org.kiji.schema.KijiURI;
import org.kiji.schema.tools.KijiToolLauncher;

import java.util.List;

/**
 * Install Tool. This tool installs a kiji instance and creates the necessary table to manage table
 * updates using the updater tools.
 *
 * @author felix.trepanier
 */
public class UpdaterInstallTool extends BaseUpdaterTool {
    @Flag(name = "kiji", usage = "URI of the Kiji instance to install,"
            + " eg. --kiji=kiji://hbase-address/kiji-instance.")
    private String kijiURIFlag = null;

    public UpdaterInstallTool() {
        this(new ZookeeperUpdaterLocker());
    }

    @VisibleForTesting
    UpdaterInstallTool(UpdaterLocker locker) {
        super(locker);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateFlags() throws Exception {
        super.validateFlags();
        Preconditions.checkArgument((kijiURIFlag != null) && !kijiURIFlag.isEmpty(),
                "Specify the instance to install with --kiji=kiji://hbase-address/kiji-instance");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected KijiURI createKijiURI() {
        return KijiURI.newBuilder(kijiURIFlag).build();
    }

    @Override
    protected int executeToolOperation(List<String> nonFlagArgs) throws Exception {
        getPrintStream().println("Installing kiji instance " + kijiURI);
        KijiInstaller.get().install(kijiURI, new Configuration());
        Kiji kiji = Kiji.Factory.open(kijiURI);

        try {
            if (KijiLayoutUpdateTableUtils.ensureLayoutUpdateTableExists(kiji)) {
                getPrintStream().println("Updater's administration table '"
                        + LayoutUpdateTable.TABLE_NAME + "' created.");
            }

            return SUCCESS;
        }
        finally {
            kiji.release();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "updater-install";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Install a new kiji instance and the updater admin tables.";
    }

    /**
     * Program entry point.
     *
     * @param args The command-line arguments.
     * @throws Exception If there is an error.
     */
    public static void main(String[] args) throws Exception {
        System.exit(new KijiToolLauncher().run(new UpdaterInstallTool(), args));
    }
}
