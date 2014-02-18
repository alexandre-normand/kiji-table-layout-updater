package com.opower.updater.admin.loader;

import com.opower.updater.admin.Update;
import org.junit.Test;

import java.io.IOException;
import java.util.SortedSet;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link com.opower.updater.admin.loader.ResourceUpdateLoader}.
 *
 * @author felix.trepanier
 */
public class TestResourceUpdateLoader {
    private static final String TABLE_NAME = "test";

    @Test
    public void testDefaultResourceLoaderLoadCreateTable() throws IOException {
        ResourceUpdateLoader loader = ResourceUpdateLoader.DEFAULT;
        Update u = loader.loadCreateTable(TABLE_NAME);

        assertEquals(0, u.getId());
        assertTrue(u.getDDL().contains("CREATE TABLE"));
    }

    @Test
    public void testDefaultResourceLoaderLoadUpdates() throws IOException {
        ResourceUpdateLoader loader = ResourceUpdateLoader.DEFAULT;
        SortedSet<Update> updates = loader.loadUpdates(TABLE_NAME);

        assertEquals(3, updates.size());
    }

    @Test(expected = TableUpdatesNotFoundException.class)
    public void testFailsIfTableUpdatesAreNotFound() throws IOException {
        ResourceUpdateLoader loader = ResourceUpdateLoader.DEFAULT;
        loader.loadUpdates("wrong");
    }

}
