/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class DbPediaHelperTest {
    @Test
    void load() {
        var record = DbPediaHelper.getInstance().getOrRead();
        assertFalse(record.programmingLanguages().isEmpty());
        assertFalse(record.markupLanguages().isEmpty());
        assertFalse(record.software().isEmpty());
    }

    @Test
    void containsAtLeastSomePopularLanguages() {
        var record = DbPediaHelper.getInstance().getOrRead();
        List<String> all = record.programmingLanguages();
        all.addAll(record.markupLanguages());
        all.addAll(record.software());
        all = all.stream().map(String::toLowerCase).toList();
        assertTrue(all.contains("python"));
        assertTrue(all.contains("javascript"));
        assertTrue(all.contains("java"));
        assertTrue(all.contains("c"));
        assertTrue(all.contains("c++"));
        assertTrue(all.contains("html"));
        assertTrue(all.contains("css"));
    }
}
