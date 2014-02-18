package com.opower.updater.admin.loader;

import com.opower.updater.admin.Update;

import java.util.Map;

/**
 * An {@link com.opower.updater.admin.loader.UpdateProcessor} that replaces tokens in the DDL given
 * a map from valid tokens to their value.
 *
 * @author felix.trepanier
 */
public class DDLTokenReplacer implements UpdateProcessor {
    public static final String TOKEN_DELIMITER = "%%%";
    private final Map<String, String> tokenMap;

    public DDLTokenReplacer(Map<String, String> tokenMap) {
        this.tokenMap = tokenMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Update processUpdate(Update update) {
        String processedDDL = update.getDDL();
        for (String token : tokenMap.keySet()) {
            processedDDL = processedDDL.replaceAll(TOKEN_DELIMITER + token + TOKEN_DELIMITER, tokenMap.get(token));
        }
        return new Update(update.getId(), processedDDL);
    }
}
