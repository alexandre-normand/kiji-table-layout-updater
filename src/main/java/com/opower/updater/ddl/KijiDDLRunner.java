package com.opower.updater.ddl;

import org.kiji.schema.shell.api.Client;

/**
 * Kiji implementation for applying DDL statement to a kiji instance.
 *
 * @author felix.trepanier
 */
public class KijiDDLRunner implements DDLRunner {

    private final Client client;

    public KijiDDLRunner(Client client) {
        this.client = client;
    }

    /**
     * Execute the DDL statements to the kiji instance.
     *
     * @param ddl The DDL statements to apply.
     */
    @Override
    public void execute(String ddl) {
        client.executeUpdate(ddl);
    }
}
