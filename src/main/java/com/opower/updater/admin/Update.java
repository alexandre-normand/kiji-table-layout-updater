package com.opower.updater.admin;

import java.util.Comparator;

/**
 * Data structure to hold and manage Update information.
 *
 * @author felix.trepanier
 */
public class Update {
    public static final Comparator<Update> UPDATE_COMPARATOR = new Comparator<Update>() {
        @Override
        public int compare(Update update1, Update update2) {
            return update1.id.compareTo(update2.id);
        }
    };

    public static Update fromUpdate(int id) {
        return new Update(id, "");
    }

    private final Integer id;
    private final String ddl;

    public Update(int id, String ddl) {
        this.id = id;
        this.ddl = ddl;
    }

    public int getId() {
        return id;
    }

    public String getDDL() {
        return ddl;
    }
}
