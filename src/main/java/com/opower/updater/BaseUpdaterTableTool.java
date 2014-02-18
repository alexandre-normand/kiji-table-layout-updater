package com.opower.updater;

import com.google.common.base.Preconditions;
import com.google.common.io.Closeables;
import com.opower.updater.admin.KijiLayoutUpdateTable;
import com.opower.updater.admin.KijiLayoutUpdateTableUtils;
import com.opower.updater.admin.LayoutUpdateTable;
import com.opower.updater.admin.loader.ResourceUpdateLoader;
import com.opower.updater.admin.loader.UpdateLoader;
import com.opower.updater.ddl.DDLRunner;
import com.opower.updater.ddl.KijiDDLRunner;
import com.opower.updater.operation.ShellClientTableDDLGenerator;
import com.opower.updater.operation.TableUpdater;
import com.opower.updater.operation.UpdateException;
import org.kiji.schema.Kiji;
import org.kiji.schema.KijiURI;
import org.kiji.schema.shell.api.Client;
import org.kiji.schema.util.ToJson;

import java.io.IOException;
import java.util.List;

/**
 * Base class for updater tools that operates on a single kiji instance table.
 *
 * @author felix.trepanier
 */
public abstract class BaseUpdaterTableTool extends BaseUpdaterTool {
    private KijiURI tableURI;
    protected Client client;

    protected Kiji kiji;
    protected KijiLayoutUpdateTable updateTable;
    protected DDLRunner ddlRunner;

    protected UpdateLoader updateLoader;

    protected BaseUpdaterTableTool(UpdaterLocker locker) {
        super(locker);
    }

    /**
     * @return The command line flag corresponding to the given table URI.
     */
    protected abstract String getTableURIFlag();

    /**
     * Execute the tool operation on the kiji table. This operation is guarded by an exclusive lock obtained from
     * zookeeper.
     *
     * @return The operation result code.
     * @throws Exception
     */
    protected abstract int executeTableOperation() throws Exception;

    /**
     * @return The update loader to use for loading updates.
     */
    protected UpdateLoader createUpdateLoader() {
        return ResourceUpdateLoader.DEFAULT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateFlags() throws Exception {
        super.validateFlags();
        String tableURIFlag = getTableURIFlag();
        Preconditions.checkArgument((tableURIFlag != null) && !tableURIFlag.isEmpty(),
                "Specify the table to create with --table=kiji://hbase-address/kiji-instance/table");
        tableURI = KijiURI.newBuilder(tableURIFlag).build();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setup() throws Exception {
        super.setup();
        kiji = Kiji.Factory.open(kijiURI, getConf());
        KijiLayoutUpdateTableUtils.ensureLayoutUpdateTableExists(kiji);

        updateTable = KijiLayoutUpdateTable.newInstance(kiji);
        client = Client.newInstance(kijiURI);
        ddlRunner = new KijiDDLRunner(client);

        //TODO todo find a way to get this from the classpath so that UpdateLoader can be customized
        updateLoader = createUpdateLoader();

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final KijiURI createKijiURI() {
        return tableURI;
    }

    /**
     * @return The name of the kiji table the tool is operating on.
     */
    protected final String getKijiTableName() {
        return kijiURI.getTable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final int executeToolOperation(List<String> nonFlagArgs) throws Exception {
        autoUpdateLayoutUpdateTable(kiji, updateTable, ddlRunner);
        try {
            return executeTableOperation();
        }
        catch (UpdateException ex) {
            getPrintStream().println("Error: Could not apply update " + ex.getFailedUpdate().getId()
                    + " for table " + ex.getTableName() + " because of '" + ex.getMessage() + "'");
            getPrintStream().println("Full error report: \n" + generateUpdateErrorReport(kiji, ex));
            return FAILURE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void cleanup() throws IOException {
        Closeables.close(client, true);
        Closeables.close(updateTable, true);
        try {
            kiji.release();
        }
        finally {
            kiji = null;
            super.cleanup();
        }
    }

    private void autoUpdateLayoutUpdateTable(Kiji kiji,
                                             KijiLayoutUpdateTable layoutUpdateTable,
                                             DDLRunner kijiDDLRunner) throws IOException {
        getPrintStream().println("Updating the internal '" + LayoutUpdateTable.TABLE_NAME + "' table.");
        new TableUpdater(kijiDDLRunner, layoutUpdateTable, kiji.getMetaTable(), new ShellClientTableDDLGenerator(client))
                .updateTable(LayoutUpdateTable.TABLE_NAME,
                        ResourceUpdateLoader.DEFAULT.loadUpdates(LayoutUpdateTable.TABLE_NAME),
                        false);
    }


    private String generateUpdateErrorReport(Kiji kiji, UpdateException updateException) throws IOException {
        return "Problematic update DDL is: \n"
                + updateException.getFailedUpdate().getDDL()
                + "\n"
                + "Table " + updateException.getTableName() + " current layout is:\n"
                + ToJson.toJsonString(kiji.getMetaTable().getTableLayout(updateException.getTableName()).getDesc());
    }


}
