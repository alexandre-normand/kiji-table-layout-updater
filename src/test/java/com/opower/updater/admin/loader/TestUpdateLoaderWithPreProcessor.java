package com.opower.updater.admin.loader;

import com.opower.updater.admin.Update;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.opower.updater.admin.loader.UpdateLoaderWithPreProcessor}.
 *
 * @author felix.trepanier
 */
public class TestUpdateLoaderWithPreProcessor {
    private static final String TABLE_NAME = "test";

    private UpdateLoader mockUpdateLoader;
    private UpdateProcessor mockPreprocessor;

    private UpdateLoaderWithPreProcessor updateLoader;

    private Update firstUpdate = new Update(0, "CREATE TABLE;");
    private Update secondUpdate = new Update(1, "ALTER TABLE;");

    @Before
    public void setup() {
        mockUpdateLoader = mock(UpdateLoader.class);
        mockPreprocessor = mock(UpdateProcessor.class);

        updateLoader = new UpdateLoaderWithPreProcessor(mockUpdateLoader, mockPreprocessor);
    }

    @Test
    public void testPreProcessorCalledOnLoadCreate() throws IOException {
        when(mockUpdateLoader.loadCreateTable(TABLE_NAME)).thenReturn(firstUpdate);
        when(mockPreprocessor.processUpdate(firstUpdate)).thenReturn(new Update(0, "processed"));

        Update update = updateLoader.loadCreateTable(TABLE_NAME);

        assertEquals(0, update.getId());
        assertEquals("processed", update.getDDL());
    }

    @Test
    public void testPreProcessorCalledOnLoadUpdates() throws IOException {
        SortedSet<Update> updates = new TreeSet<Update>(Update.UPDATE_COMPARATOR);
        updates.add(firstUpdate);
        updates.add(secondUpdate);

        when(mockUpdateLoader.loadUpdates(TABLE_NAME)).thenReturn(updates);
        when(mockPreprocessor.processUpdate(firstUpdate)).thenReturn(new Update(0, "processed"));
        when(mockPreprocessor.processUpdate(secondUpdate)).thenReturn(new Update(1, "processed again"));

        SortedSet<Update> processedUpdates = updateLoader.loadUpdates(TABLE_NAME);

        assertEquals(0, processedUpdates.first().getId());
        assertEquals("processed", processedUpdates.first().getDDL());

        assertEquals(1, processedUpdates.last().getId());
        assertEquals("processed again", processedUpdates.last().getDDL());
    }
}
