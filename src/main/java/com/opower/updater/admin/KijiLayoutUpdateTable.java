package com.opower.updater.admin;

import com.google.common.io.Closeables;
import com.opower.updater.LayoutUpdate;
import org.apache.hadoop.hbase.HConstants;
import org.kiji.schema.EntityId;
import org.kiji.schema.Kiji;
import org.kiji.schema.KijiDataRequest;
import org.kiji.schema.KijiDataRequestBuilder;
import org.kiji.schema.KijiRowData;
import org.kiji.schema.KijiTable;
import org.kiji.schema.KijiTableReader;
import org.kiji.schema.KijiTableWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Kiji based implementation of {@link com.opower.updater.admin.LayoutUpdateTable}.
 * <p/>
 * The method {@link KijiLayoutUpdateTable#newInstance(org.kiji.schema.Kiji)} must be used to
 * create a new instance.
 *
 * @author felix.trepanier
 */
public final class KijiLayoutUpdateTable implements LayoutUpdateTable {
    private static final KijiDataRequest LATEST_UPDATE_REQUEST = KijiDataRequest.create(
            UPDATE_LOG_FAMILY_NAME,
            LAYOUT_UPDATE_COLUMN_NAME);

    private static final KijiDataRequest FULL_TABLE_HISTORY_REQUEST = KijiDataRequest.builder()
            .addColumns(KijiDataRequestBuilder.ColumnsDef.create()
                    .withMaxVersions(HConstants.ALL_VERSIONS)
                    .add(UPDATE_LOG_FAMILY_NAME,
                            LAYOUT_UPDATE_COLUMN_NAME)).build();

    private final KijiTable layoutUpdateTable;
    private final KijiTableReader reader;
    private final KijiTableWriter writer;

    /**
     * Create an instance of the {@link KijiLayoutUpdateTable}.
     *
     * @param kiji The kiji instance.
     * @return A new instance of {@link KijiLayoutUpdateTable}
     * @throws IOException
     */
    public static KijiLayoutUpdateTable newInstance(Kiji kiji) throws IOException {
        return new KijiLayoutUpdateTable(kiji.openTable(TABLE_NAME));
    }

    private KijiLayoutUpdateTable(KijiTable layoutUpdateTable) {
        this.layoutUpdateTable = layoutUpdateTable;
        this.reader = layoutUpdateTable.openTableReader();
        this.writer = layoutUpdateTable.openTableWriter();
    }

    @Override
    public Integer getLastUpdateIdForTable(String tableName) throws IOException {
        KijiRowData data = reader.get(createEntityId(tableName), LATEST_UPDATE_REQUEST);
        LayoutUpdate latestUpdate = data.getMostRecentValue(UPDATE_LOG_FAMILY_NAME, LAYOUT_UPDATE_COLUMN_NAME);
        return latestUpdate == null ? null : latestUpdate.getUpdateId();
    }

    @Override
    public void insertLayoutUpdate(String tableName, LayoutUpdate layoutUpdate) throws IOException {
        writer.put(createEntityId(tableName), UPDATE_LOG_FAMILY_NAME, LAYOUT_UPDATE_COLUMN_NAME, layoutUpdate);
        writer.flush();
    }

    @Override
    public Collection<LayoutUpdate> getTableHistory(String tableName, Integer numberOfVersions) throws IOException {
        KijiRowData data = reader.get(createEntityId(tableName), buildKijiDataRequest(numberOfVersions));
        Map<Long, LayoutUpdate> rows = data.getValues(UPDATE_LOG_FAMILY_NAME, LAYOUT_UPDATE_COLUMN_NAME);
        return rows.values();
    }

    @Override
    public void close() throws IOException {
        Closeables.close(reader, true);
        Closeables.close(writer, true);
        layoutUpdateTable.release();
    }

    private EntityId createEntityId(String tableName) {
        return layoutUpdateTable.getEntityId(tableName);
    }

    private KijiDataRequest buildKijiDataRequest(Integer numberOfVersions) throws IOException {
        if (numberOfVersions == null) {
            return FULL_TABLE_HISTORY_REQUEST;
        }
        else {
            return KijiDataRequest.builder()
                    .addColumns(KijiDataRequestBuilder.ColumnsDef.create()
                            .withMaxVersions(numberOfVersions)
                            .add(UPDATE_LOG_FAMILY_NAME,
                                    LAYOUT_UPDATE_COLUMN_NAME)).build();
        }
    }
}
