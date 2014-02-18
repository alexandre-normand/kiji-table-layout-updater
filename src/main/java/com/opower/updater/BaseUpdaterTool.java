package com.opower.updater;

import com.opower.updater.admin.loader.TableUpdatesNotFoundException;
import com.opower.updater.operation.MissingUpdateException;
import com.opower.updater.operation.OperationForbiddenException;
import com.opower.updater.operation.TableAlreadyExistException;
import com.opower.updater.operation.TableDoesNotExistException;
import org.kiji.schema.KijiURI;
import org.kiji.schema.tools.BaseTool;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Base class for updater tools. This base class is responsible for obtaining a lock in zookeeper to avoid
 * running two tools that could alter the tables in a given kiji instance at the same time.
 *
 * @author felix.trepanier
 */
public abstract class BaseUpdaterTool extends BaseTool {
    private static final Integer MAX_LOCK_WAIT_TIME_SEC = 5;

    private final UpdaterLocker locker;
    protected KijiURI kijiURI;

    protected BaseUpdaterTool(UpdaterLocker locker) {
        this.locker = locker;
    }

    /**
     * Executes the tool operation.
     * <p/>
     * This is called by the {@link com.opower.updater.BaseUpdaterTool#run(java.util.List)} method
     * after the updater lock has been acquired.
     *
     * @param nonFlagArgs The arguments on the command-line that were not parsed as flags.
     * @return Operation return code.
     * @throws Exception
     */
    protected abstract int executeToolOperation(List<String> nonFlagArgs) throws Exception;

    /**
     * Create the KijiURI to be used to open the kiji instance.
     *
     * @return the KijiURI to be used to open the kiji instance.
     */
    protected abstract KijiURI createKijiURI();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setup() throws Exception {
        super.setup();
        kijiURI = createKijiURI();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final int run(List<String> nonFlagArgs) throws Exception {
        UpdaterLocker.AcquiredLock lock = null;
        try {
            lock = locker.acquireLock(kijiURI, MAX_LOCK_WAIT_TIME_SEC, TimeUnit.SECONDS);
            return executeToolOperation(nonFlagArgs);
        }
        catch (LockNotAcquiredException ex) {
            getPrintStream().println("Error: Could not acquire the updater lock: " + ex.getMessage() + ".");
            return FAILURE;
        }
        catch (OperationForbiddenException ex) {
            getPrintStream().println("Error: " + ex.getMessage());
            return FAILURE;
        }
        catch (TableAlreadyExistException ex) {
            getPrintStream().println("Error: Table '" + ex.getTableName() + "' already exists.");
            return FAILURE;
        }
        catch (TableDoesNotExistException ex) {
            getPrintStream().println("Error: Table '" + ex.getTableName() + "' does not exist.");
            return FAILURE;
        }
        catch (MissingUpdateException ex) {
            getPrintStream().println("Error: Update " + ex.getExpectedId() + " was not found."
                    + " Id " + ex.getIdFound() + " was found instead.");
            return FAILURE;
        }
        catch (TableUpdatesNotFoundException ex) {
            getPrintStream().println("Error: Could not find updates for table '" + ex.getTableName() + "'.");
            return FAILURE;
        }
        finally {
            if (lock != null) {
                lock.release();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCategory() {
        return "updater";
    }
}
