/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class ConfusablesHelperTest {
    public static final String EXAMPLE = """
            #	!	ǃ	ⵑ	！
            """;
    public static final List<UnicodeCharacter> homoglyphsExample = Stream.of("!", "ǃ", "ⵑ", "！").map(UnicodeCharacter::valueOf).toList();

    @Test
    void extractHomoglyphsFromLine() {
        var homoglyphs = ConfusablesHelper.extractHomoglyphsFromLine(EXAMPLE);
        assertEquals(homoglyphsExample.size(), homoglyphs.size());
        assertTrue(homoglyphs.containsAll(homoglyphsExample));
    }

    @Test
    void getHomoglyphs() {
        var homoglyphs = ConfusablesHelper.getHomoglyphs(homoglyphsExample.get(0));
        assertTrue(homoglyphs.size() >= homoglyphsExample.size());
        assertTrue(homoglyphs.containsAll(homoglyphsExample));
    }

    @Test
    void areHomoglyphs() {
        assertTrue(ConfusablesHelper.areHomoglyphs(UnicodeCharacter.valueOf("!"), UnicodeCharacter.valueOf("！")));
    }
}
