package com.opower.updater.admin;

import com.opower.updater.admin.loader.ResourceUpdateLoader;
import com.opower.updater.ddl.DDLRunner;
import com.opower.updater.ddl.KijiDDLRunner;
import org.kiji.schema.Kiji;
import org.kiji.schema.shell.api.Client;

import java.io.IOException;

/**
 * Helper method to enforce the presence of the 'layout_update' table in the kiji instance.
 *
 * @author felix.trepanier
 */
public final class KijiLayoutUpdateTableUtils {

    private KijiLayoutUpdateTableUtils() {
    }

    /**
     * Create the layout_update table if it does not exist.
     *
     * @param kiji The kiji instance.
     * @return Returns true if the table was created, false otherwise.
     * @throws IOException
     */
    public static boolean ensureLayoutUpdateTableExists(Kiji kiji) throws IOException {
        if (!kiji.getMetaTable().tableExists(LayoutUpdateTable.TABLE_NAME)) {
            Client client = Client.newInstance(kiji.getURI());

            try {
                Update initialUpdate = ResourceUpdateLoader.DEFAULT.loadCreateTable(LayoutUpdateTable.TABLE_NAME);

                DDLRunner runner = new KijiDDLRunner(client);
                LayoutUpdateTableCreator creator = new LayoutUpdateTableCreator(runner, kiji);
                creator.createLayoutUpdateTable(initialUpdate);
            }
            finally {
                client.close();
            }
            return true;
        }
        else {
            return false;
        }
    }
}
