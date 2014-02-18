package com.opower.updater.admin.loader;

import com.opower.updater.admin.Update;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Update loader that loads updates from resources in the classpath. This class needs to be extended to provide the
 * definition for the two template methods {@link #buildUpdateFilePattern(String)} and
 * {@link #extractIdFromFileName(String)}.
 *
 * @author felix.trepanier
 */
public abstract class ResourceUpdateLoader implements UpdateLoader {

    /**
     * Default implementation.
     * <p/>
     * It finds files that matches the pattern [numbers]-[tableName].ddl in the kiji.schema.[tableName]
     * package.
     */
    public static final ResourceUpdateLoader DEFAULT = new ResourceUpdateLoader("kiji.schema.") {
        @Override
        protected String buildUpdateFilePattern(String tableName) {
            return "\\d+\\-" + tableName + "\\.ddl";
        }

        @Override
        protected int extractIdFromFileName(String filename) {
            return Integer.decode(filename.split("-")[0]);
        }
    };

    private final String packagePrefix;

    /**
     * Constructor for the {@link com.opower.updater.admin.loader.ResourceUpdateLoader}. The loader will only
     * look for update files contained in the specified package prefix followed by the table name.
     *
     * @param packagePrefix Package prefix used to build the full package where the update files are located.
     */
    protected ResourceUpdateLoader(String packagePrefix) {
        this.packagePrefix = packagePrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Update loadCreateTable(String tableName) throws IOException, TableUpdatesNotFoundException {
        return loadUpdates(tableName).first();
    }

    /**
     * This method assumes that the DDL updates are located in the classpath
     * under the folder defined by {@link #packagePrefix} The updates files must respect the
     * pattern returned by {@link #buildUpdateFilePattern(String)}.
     *
     * @param tableName The name of the table to load updates for.
     * @return The table updates.
     * @throws java.io.IOException           If an I/O error occurs
     * @throws TableUpdatesNotFoundException If updates for the given tables are not found.
     */
    @Override
    public SortedSet<Update> loadUpdates(String tableName) throws IOException, TableUpdatesNotFoundException {
        Reflections reflections = new Reflections(packagePrefix + tableName, new ResourcesScanner());
        Set<String> resources = reflections.getResources(Pattern.compile(buildUpdateFilePattern(tableName)));

        if (resources.isEmpty()) {
            throw new TableUpdatesNotFoundException(tableName);
        }

        SortedSet<Update> updates = new TreeSet<Update>(Update.UPDATE_COMPARATOR);
        for (String resource : resources) {
            int id = extractIdFromFileName(new File(resource).getName());
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(resource)));

            String line;
            StringBuilder ddl = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                ddl.append(line).append("\n");
            }
            updates.add(new Update(id, ddl.toString()));
        }

        return updates;
    }

    /**
     * Construct the resource file pattern from the table name. The loader will only load files that matches the given
     * pattern.
     *
     * @param tableName The name of the table to load the updates for.
     * @return The update file pattern.
     */
    protected abstract String buildUpdateFilePattern(String tableName);

    /**
     * Extract the update id from the file name.
     *
     * @param filename The update file name.
     * @return The update id.
     */
    protected abstract int extractIdFromFileName(String filename);
}
