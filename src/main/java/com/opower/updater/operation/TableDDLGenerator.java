package com.opower.updater.operation;

/**
 * Interface used to retrieve the table current layout as DDL statements;
 *
 * @author felix.trepanier
 */
public interface TableDDLGenerator {

    /**
     * Generate the current table layout as DDL statements.
     *
     * @param tableName The table name
     * @return The current layout as DDL statements.
     */
    String generateTableLayoutDDL(String tableName);
}
