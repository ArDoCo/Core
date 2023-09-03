package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacter;

public class LevenshteinMeasureTest {
    private static final double delta = 0.01;

    /**
     * These tests were extracted from the original {@link org.apache.commons.text.similarity.LevenshteinDistance} implementation and should still hold true.
     */
    @Test
    void testSimilarityDirectly() {
        var distance = new UnicodeLevenshteinDistance();
        assertThrows(IllegalArgumentException.class, () -> distance.apply(null, "foo", UnicodeCharacter.EQUAL));
        assertThrows(IllegalArgumentException.class, () -> distance.apply("foo", null, UnicodeCharacter.EQUAL));
        assertEquals(0, distance.apply("", "", UnicodeCharacter.EQUAL));
        assertEquals(1, distance.apply("", "a", UnicodeCharacter.EQUAL));
        assertEquals(7, distance.apply("aaapppp", "", UnicodeCharacter.EQUAL));
        assertEquals(1, distance.apply("frog", "fog", UnicodeCharacter.EQUAL));
        assertEquals(3, distance.apply("fly", "ant", UnicodeCharacter.EQUAL));
        assertEquals(7, distance.apply("elephant", "hippo", UnicodeCharacter.EQUAL));
        assertEquals(7, distance.apply("hippo", "elephant", UnicodeCharacter.EQUAL));
        assertEquals(8, distance.apply("hippo", "zzzzzzzz", UnicodeCharacter.EQUAL));
        assertEquals(1, distance.apply("hello", "hallo", UnicodeCharacter.EQUAL));
    }

    @Test
    void testHomoglyphSimilarity() {
        var measure = new LevenshteinMeasure();
        assertEquals(1d, measure.getSimilarity(new ComparisonContext("ℜ𝘂ᖯʏ", "Ruby", UnicodeCharacter.EQUAL_OR_HOMOGLYPH)), delta);
    }
}
