package com.opower.updater.ddl;

/**
 * Interface used to apply DDL statements on a kiji instance.
 *
 * @author felix.trepanier
 */
public interface DDLRunner {

    /**
     * Execute the DDL statements.
     *
     * @param ddl The DDL statements to apply.
     */
    void execute(String ddl);
}
