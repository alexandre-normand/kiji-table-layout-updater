package com.opower.updater.admin.loader;

import com.opower.updater.admin.Update;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link com.opower.updater.admin.loader.DDLTokenReplacer}.
 *
 * @author felix.trepanier
 */
public class TestDDLTokenReplacer {

    @Test
    public void testTokensAreReplaced() {
        String token = "token";
        String value = "value";
        Map<String, String> tokenMap = new HashMap<String, String>();
        tokenMap.put(token, value);
        DDLTokenReplacer preProcessor = new DDLTokenReplacer(tokenMap);

        Update update = preProcessor.processUpdate(new Update(0, "CREATE TABLE WITH TOKEN = "
                + DDLTokenReplacer.TOKEN_DELIMITER + token + DDLTokenReplacer.TOKEN_DELIMITER));

        assertEquals(0, update.getId());
        assertTrue(update.getDDL().contains(value));
        assertFalse(update.getDDL().contains(DDLTokenReplacer.TOKEN_DELIMITER
                + token + DDLTokenReplacer.TOKEN_DELIMITER));
    }
}
