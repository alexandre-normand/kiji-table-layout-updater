package com.opower.updater;

import com.opower.updater.admin.KijiLayoutUpdateTable;
import com.opower.updater.admin.KijiLayoutUpdateTableUtils;
import com.opower.updater.admin.LayoutUpdateTable;
import com.opower.updater.admin.loader.DDLTokenReplacer;
import com.opower.updater.ddl.KijiDDLRunner;
import com.opower.updater.operation.TableCreator;
import org.junit.Test;
import org.kiji.schema.shell.api.Client;
import org.kiji.schema.tools.BaseTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Tests for {@link com.opower.updater.UpdaterUpdateTool}.
 *
 * @author felix.trepanier
 */
public class TestUpdaterUpdateTool extends UpdaterToolTest {
    @Test
    public void testUpdateFailsItTableDoesNotExists() throws Exception {
        assertEquals(BaseTool.FAILURE, runTool(updateTool(), "--table=" + tableURI));

        checkLastPrintedLineIsAnError();
    }

    @Test
    public void testUpdateFailsIfTableDoesNotExistInLayoutUpdateAndBootstrapIsFalse() throws Exception {
        Client client = Client.newInstance(getKiji().getURI());
        client.executeUpdate(loader.loadCreateTable(TABLE_NAME)
                .getDDL()
                .replaceAll(DDLTokenReplacer.TOKEN_DELIMITER
                        + UpdaterCreateTool.NUM_REGIONS_TOKEN
                        + DDLTokenReplacer.TOKEN_DELIMITER,
                        "1"));

        assertEquals(BaseTool.FAILURE, runTool(updateTool(), "--table=" + tableURI));
        checkLastPrintedLineIsAnError();

        client.close();
    }

    @Test
    public void testUpdateInsertInitialLayoutIfTableAlreadyExistInKijiInstanceButNotInLayoutUpdateTable()
            throws Exception {
        String createDDL = loader.loadCreateTable("test")
                .getDDL()
                .replaceAll(DDLTokenReplacer.TOKEN_DELIMITER
                        + UpdaterCreateTool.NUM_REGIONS_TOKEN
                        + DDLTokenReplacer.TOKEN_DELIMITER,
                        "1");

        Client client = Client.newInstance(getKiji().getURI());
        client.executeUpdate(createDDL);

        assertEquals(BaseTool.SUCCESS, runTool(updateTool(), "--table=" + tableURI, "--bootstrap=true"));

        client.close();

        assertTestTableComplete();

        Collection<LayoutUpdate> history =
                KijiLayoutUpdateTable.newInstance(getKiji()).getTableHistory(tableURI.getTable(), null);

        // history should contain the bootstrapped DDL and the 2 updates
        assertEquals(3, history.size());
        // the first update should be the create table statement
        assertTrue(new ArrayList<LayoutUpdate>(history).get(2).getAppliedDDL().contains("CREATE TABLE"));
    }

    @Test
    public void testUpdate() throws Exception {
        createTestTable();

        assertEquals(BaseTool.SUCCESS, runTool(updateTool(), "--table=" + tableURI));

        assertTestTableComplete();
    }

    private void createTestTable() throws IOException {
        KijiLayoutUpdateTableUtils.ensureLayoutUpdateTableExists(getKiji());
        LayoutUpdateTable updateTable = KijiLayoutUpdateTable.newInstance(getKiji());
        Client client = Client.newInstance(getKiji().getURI());

        Map<String, String> tokenMap = new HashMap<String, String>(1);
        tokenMap.put(UpdaterCreateTool.NUM_REGIONS_TOKEN, "1");

        DDLTokenReplacer tokenReplacer = new DDLTokenReplacer(tokenMap);

        TableCreator creator;
        try {
            creator = new TableCreator(new KijiDDLRunner(client), updateTable, getKiji().getMetaTable());
            creator.createTable("test", tokenReplacer.processUpdate(loader.loadCreateTable(TABLE_NAME)));
        }
        finally {
            updateTable.close();
            client.close();
        }
    }

    private UpdaterUpdateTool updateTool() {
        return new UpdaterUpdateTool(fakeUpdaterLocker);
    }
}
