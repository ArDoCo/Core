package edu.kit.kastel.mcse.ardoco.core.common.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class DBPediaHelperTest {
    @Test
    void load() {
        var record = DBPediaHelper.getInstance().load();
        assertTrue(record.programmingLanguages().size() > 0);
        assertTrue(record.markupLanguages().size() > 0);
    }

    @Test
    void containsAtLeastSomePopularLanguages() {
        var record = DBPediaHelper.getInstance().load();
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
