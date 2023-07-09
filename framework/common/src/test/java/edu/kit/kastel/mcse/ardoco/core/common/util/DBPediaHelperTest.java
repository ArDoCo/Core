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
        assertTrue(record.programmingLanguages()
                .stream()
                .map(String::toLowerCase)
                .toList()
                .containsAll(List.of("python", "javascript", "java", "c", "c++", "html", "css", "scss")));
    }
}
