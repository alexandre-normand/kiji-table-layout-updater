package com.opower.updater;

import com.opower.updater.admin.loader.ResourceUpdateLoader;
import com.opower.updater.admin.loader.UpdateLoader;
import org.junit.Before;
import org.kiji.schema.KijiTable;
import org.kiji.schema.KijiURI;
import org.kiji.schema.layout.KijiTableLayout;
import org.kiji.schema.tools.KijiToolTest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Base test class for updater tools.
 *
 * @author felix.trepanier
 */
public class UpdaterToolTest extends KijiToolTest {

    protected static final String TABLE_NAME = "test";
    protected KijiURI tableURI;
    protected UpdateLoader loader = ResourceUpdateLoader.DEFAULT;
    protected UpdaterLocker fakeUpdaterLocker;


    @Before
    public void setup() throws Exception {
        fakeUpdaterLocker = new FakeUpaterLocker();
        tableURI = KijiURI.newBuilder(getKiji().getURI()).withTableName(TABLE_NAME).build();
    }

    protected void assertTestTableComplete() throws IOException {
        KijiTable table = getKiji().openTable("test");
        KijiTableLayout.LocalityGroupLayout.FamilyLayout family = table.getLayout().getFamilies().iterator().next();

        assertEquals("test", family.getName());
        assertEquals(3, family.getColumns().size());
        assertTrue(family.getColumnMap().containsKey("stuff"));
        assertTrue(family.getColumnMap().containsKey("moreStuff"));
        assertTrue(family.getColumnMap().containsKey("yetAnother"));
    }

    protected void checkLastPrintedLineIsAnError() {
        assertTrue(mToolOutputLines[mToolOutputLines.length - 1].startsWith("Error"));
    }

    private static class FakeUpaterLocker implements UpdaterLocker {

        @Override
        public AcquiredLock acquireLock(KijiURI kijiURI, int maxWaitTime, TimeUnit timeUnit) throws LockNotAcquiredException {
            return new AcquiredLock() {
                @Override
                public void release() throws Exception {

                }
            };
        }
    }
}
