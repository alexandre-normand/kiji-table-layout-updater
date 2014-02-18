kiji-table-layout-updater
=========================

This updater applies table layout updates to a specific kiji instance. It enforces that all updates are applied once,
in the right order and that the resulting table layout conforms to the expected layout using a validation function
(e.g. checksum).

The updater further logs all layout updates to a kiji table (layout_update) to save the current state of the layout of each
tables, but also to keep the history of all applied DDL using the tool on the instance.

The updater features are divided in 5 tools that can be run using the kiji tool. To use the updater tool, set the
kiji-table-layout-updater-*-tools.jar in the KIJI_CLASSPATH prior to running the kiji command.

The 5 tools are
- updater-install: Install a new kiji instance and the updater admin tables.
- updater-create: Create a table and apply updates.
- updater-update: Apply updates to a given table.
- updater-drop: Drop a table.
- updater-history: Print the update history for a given table.

At the moment, the update files must be stored in a resource (bundled in a jar that must also be added to KIJI_CLASSPATH)
in the package kiji.schema.<table_name>. The update files name must follow the pattern <id>-<table_name>.ddl.
The id must be numeric (but can be prefixed with zeros, e.g. 000, 01, 276, etc) and unique. The first update
(the one containing the CREATE TABLE) must have the id 0. Each of the following update ids must be previous id
increased by one.

If the execution of one of the update fails, the whole update process is aborted and an error report is generated.
If the failing update contained more than one DDL statement, the table might have to be manually
rolled back if some of the statement were executed. Thus, it is recommended that each update file contains
only a single DDL statement.
