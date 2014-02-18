package com.opower.updater.admin;

import com.opower.updater.LayoutUpdate;
import com.opower.updater.ddl.DDLRunner;
import com.opower.updater.operation.TableAlreadyExistException;
import com.opower.updater.operation.TableOperator;
import org.kiji.schema.Kiji;

import java.io.IOException;

/**
 * Table creator for the 'layout_update' table.
 * <p/>
 * The generic table creator could not be reused since it requires that the 'layout_update' table exists.
 *
 * @author felix.trepanier
 */
public class LayoutUpdateTableCreator extends TableOperator {
    private final Kiji kiji;

    public LayoutUpdateTableCreator(DDLRunner ddlRunner, Kiji kiji) throws IOException {
        super(ddlRunner, null, kiji.getMetaTable());
        this.kiji = kiji;
    }

    /**
     * Create the 'layout_update' table.
     *
     * @param initialUpdate The initial update that creates the table.
     * @throws IOException
     * @throws TableAlreadyExistException The layout_update table already exist in the instance.
     */
    public void createLayoutUpdateTable(Update initialUpdate) throws IOException {
        if (metaTable.tableExists(LayoutUpdateTable.TABLE_NAME)) {
            throw new TableAlreadyExistException(LayoutUpdateTable.TABLE_NAME,
                    "Table " + LayoutUpdateTable.TABLE_NAME + "already exists in the kiji instance.");
        }
        LayoutUpdate layoutUpdate = applyUpdate(initialUpdate);
        KijiLayoutUpdateTable layoutUpdateTable = KijiLayoutUpdateTable.newInstance(kiji);
        try {
            layoutUpdateTable.insertLayoutUpdate(LayoutUpdateTable.TABLE_NAME, layoutUpdate);
        }
        finally {
            layoutUpdateTable.close();
        }
    }

}
