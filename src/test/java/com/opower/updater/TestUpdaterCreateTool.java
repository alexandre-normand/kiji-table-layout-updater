package com.opower.updater;

import com.opower.updater.admin.LayoutUpdateTable;
import org.junit.Test;
import org.kiji.schema.KijiDataRequest;
import org.kiji.schema.KijiDataRequestBuilder;
import org.kiji.schema.KijiRowData;
import org.kiji.schema.KijiTable;
import org.kiji.schema.KijiTableReader;
import org.kiji.schema.KijiURI;
import org.kiji.schema.avro.CompressionType;
import org.kiji.schema.layout.KijiTableLayout;
import org.kiji.schema.tools.BaseTool;

import java.util.Map;
import java.util.NavigableMap;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for {@link com.opower.updater.UpdaterCreateTool}.
 *
 * @author felix.trepanier
 */
public class TestUpdaterCreateTool extends UpdaterToolTest {
    @Test
    public void testCreateActionCreatesTheTableAndUpdateIt() throws Exception {
        assertEquals(BaseTool.SUCCESS, runTool(createTool(), "--table=" + tableURI));

        assertTestTableComplete();
    }

    @Test
    public void testCreateActionWithATableThatIsNotDefined() throws Exception {
        assertEquals(BaseTool.FAILURE, runTool(createTool(),
                "--table=" + KijiURI.newBuilder(getKiji().getURI()).withTableName("wrong").build()));

        checkLastPrintedLineIsAnError();
    }

    @Test
    public void testCreateActionCreatesWithNumRegions() throws Exception {
        Integer numRegions = 256;
        assertEquals(BaseTool.SUCCESS, runTool(createTool(),
                "--table=" + tableURI,
                "--num-regions=" + numRegions.toString()));

        assertEquals(numRegions.intValue(), getKiji().openTable("test").getRegions().size());
    }

    @Test
    public void testCreateActionWithInvalidCompressionFails() throws Exception {
        try {
            assertEquals(
                    BaseTool.SUCCESS, runTool(
                            createTool(),
                            "--table=" + tableURI,
                            "--compression=" + "bad"));
            fail("Expected IllegalArgumentException with invalid compression type.");
        }
        catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Invalid compression type [bad], must be one of [NONE, GZ, LZO, SNAPPY]"));
        }
    }

    @Test
    public void testCreateActionWithLowercaseCompressionTypeSucceeds() throws Exception {
        assertEquals(BaseTool.SUCCESS, runTool(createTool(),
                        "--table=" + tableURI,
                        "--compression=" + "gzip"));

        for (KijiTableLayout.LocalityGroupLayout localityGroup : getKiji().openTable("test").getLayout().getLocalityGroups()) {
            assertThat(localityGroup.getDesc().getCompressionType(), is(CompressionType.GZ));
        }
    }

    @Test
    public void testCreateActionWithCompressionTypeSetsCompression() throws Exception {
        assertEquals(BaseTool.SUCCESS, runTool(createTool(),
                        "--table=" + tableURI,
                        "--compression=" + "LZO"));

        for (KijiTableLayout.LocalityGroupLayout localityGroup : getKiji().openTable("test").getLayout().getLocalityGroups()) {
            assertThat(localityGroup.getDesc().getCompressionType(), is(CompressionType.LZO));
        }
    }

    @Test
    public void testLayoutUpdatesAreRecordedInLayoutUpdateTable() throws Exception {
        assertEquals(BaseTool.SUCCESS, runTool(createTool(), "--table=" + tableURI));

        KijiTable table = getKiji().openTable(LayoutUpdateTable.TABLE_NAME);
        KijiTableReader reader = table.openTableReader();

        KijiDataRequest request = KijiDataRequest.builder()
                .addColumns(KijiDataRequestBuilder.ColumnsDef.create()
                        .withMaxVersions(3)
                        .add(LayoutUpdateTable.UPDATE_LOG_FAMILY_NAME,
                                LayoutUpdateTable.LAYOUT_UPDATE_COLUMN_NAME)).build();
        KijiRowData data = reader.get(table.getEntityId("test"), request);

        NavigableMap<Long, LayoutUpdate> updates = data.getValues(LayoutUpdateTable.UPDATE_LOG_FAMILY_NAME,
                LayoutUpdateTable.LAYOUT_UPDATE_COLUMN_NAME);

        Map.Entry<Long, LayoutUpdate> firstEntry = updates.firstEntry();
        Map.Entry<Long, LayoutUpdate> secondEntry = updates.higherEntry(firstEntry.getKey());
        Map.Entry<Long, LayoutUpdate> thirdEntry = updates.lastEntry();

        assertEquals(new Integer(2), firstEntry.getValue().getUpdateId());
        assertEquals(new Integer(1), secondEntry.getValue().getUpdateId());
        assertEquals(new Integer(0), thirdEntry.getValue().getUpdateId());
    }

    @Test
    public void testLayoutIdIsSetAfterUpdate() throws Exception {
        assertEquals(BaseTool.SUCCESS, runTool(createTool(), "--table=" + tableURI, "--set-layout-id=true"));

        assertEquals("test-layout-id-2", getKiji().getMetaTable().getTableLayout(tableURI.getTable()).getDesc().getLayoutId());
    }

    @Test
    public void testLayoutIdIsNotSetAfterUpdateIfFlagTurnedOff() throws Exception {
        assertEquals(BaseTool.SUCCESS, runTool(createTool(), "--table=" + tableURI, "--set-layout-id=false"));

        assertThat("test-layout-id-2",
                not(equalTo(getKiji().getMetaTable().getTableLayout(tableURI.getTable()).getDesc().getLayoutId())));
    }

    private UpdaterCreateTool createTool() {
        return new UpdaterCreateTool(fakeUpdaterLocker);
    }
}
