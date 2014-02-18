package com.opower.updater.admin.loader;

import com.opower.updater.admin.Update;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * An {@link com.opower.updater.admin.loader.UpdateLoader} implementation that allows pre-processing of the updates
 * before applying them.
 *
 * @author felix.trepanier
 */
public class UpdateLoaderWithPreProcessor implements UpdateLoader {
    private final UpdateLoader updateLoader;
    private final UpdateProcessor preProcessor;

    /**
     * Construct a {@link com.opower.updater.admin.loader.UpdateLoaderWithPreProcessor} with the given
     * {@link com.opower.updater.admin.loader.UpdateLoader} and {@link UpdateProcessor}.
     *
     * @param updateLoader Update loader used to load the updates.
     * @param preProcessor The preprocessor used to pre-process each updates.
     */
    public UpdateLoaderWithPreProcessor(UpdateLoader updateLoader, UpdateProcessor preProcessor) {
        this.updateLoader = updateLoader;
        this.preProcessor = preProcessor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Update loadCreateTable(String tableName) throws IOException {
        return preProcessor.processUpdate(updateLoader.loadCreateTable(tableName));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Update> loadUpdates(String tableName) throws IOException {
        SortedSet<Update> preProcessedUpdates = new TreeSet<Update>(Update.UPDATE_COMPARATOR);
        for (Update update : updateLoader.loadUpdates(tableName)) {
            preProcessedUpdates.add(preProcessor.processUpdate(update));
        }
        return preProcessedUpdates;
    }
}
