package com.opower.updater.operation;

import org.kiji.schema.shell.api.Client;

/**
 * Implementation of the {@link TableDDLGenerator} that uses the kiji client shell
 * API to get the table current layout as DDL statements.
 *
 * @author felix.trepanier
 */
public class ShellClientTableDDLGenerator implements TableDDLGenerator {
    private final Client client;

    public ShellClientTableDDLGenerator(Client client) {
        this.client = client;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateTableLayoutDDL(String tableName) {
        client.executeUpdate("DUMP DDL FOR TABLE " + tableName + ";");
        return client.getLastOutput();
    }
}
