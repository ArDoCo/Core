/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacterMatchFunctions;

public class LevenshteinMeasureTest {
    private static final double delta = 0.01;

    /**
     * These tests were extracted from the original {@link org.apache.commons.text.similarity.LevenshteinDistance} implementation and should still hold true.
     */
    @Test
    void testSimilarityDirectly() {
        var distance = new UnicodeLevenshteinDistance();
        assertThrows(IllegalArgumentException.class, () -> distance.apply(null, "foo", UnicodeCharacterMatchFunctions.EQUAL));
        assertThrows(IllegalArgumentException.class, () -> distance.apply("foo", null, UnicodeCharacterMatchFunctions.EQUAL));
        assertEquals(0, distance.apply("", "", UnicodeCharacterMatchFunctions.EQUAL));
        assertEquals(1, distance.apply("", "a", UnicodeCharacterMatchFunctions.EQUAL));
        assertEquals(7, distance.apply("aaapppp", "", UnicodeCharacterMatchFunctions.EQUAL));
        assertEquals(1, distance.apply("frog", "fog", UnicodeCharacterMatchFunctions.EQUAL));
        assertEquals(3, distance.apply("fly", "ant", UnicodeCharacterMatchFunctions.EQUAL));
        assertEquals(7, distance.apply("elephant", "hippo", UnicodeCharacterMatchFunctions.EQUAL));
        assertEquals(7, distance.apply("hippo", "elephant", UnicodeCharacterMatchFunctions.EQUAL));
        assertEquals(8, distance.apply("hippo", "zzzzzzzz", UnicodeCharacterMatchFunctions.EQUAL));
        assertEquals(1, distance.apply("hello", "hallo", UnicodeCharacterMatchFunctions.EQUAL));
    }

    @Test
    void testHomoglyphSimilarity() {
        var measure = new LevenshteinMeasure();
        assertEquals(1d, measure.getSimilarity(new ComparisonContext("ℜ𝘂ᖯʏ", "Ruby", UnicodeCharacterMatchFunctions.EQUAL_OR_HOMOGLYPH)), delta);
    }
}
