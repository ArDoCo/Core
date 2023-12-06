/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacterMatchFunctions;

public class JaroWinklerMeasureTest {
    private static final double delta = 0.01;

    /**
     * These tests were extracted from the original {@link org.apache.commons.text.similarity.JaroWinklerSimilarity} implementation and should
     * still hold true.
     */
    @Test
    void testSimilarityDirectly() {
        String s = null;
        assertThrows(IllegalArgumentException.class, () -> UnicodeJaroWinklerSimilarity.apply(s, s, UnicodeCharacterMatchFunctions.EQUAL));
        assertThrows(IllegalArgumentException.class, () -> UnicodeJaroWinklerSimilarity.apply("foo", null, UnicodeCharacterMatchFunctions.EQUAL));
        assertThrows(IllegalArgumentException.class, () -> UnicodeJaroWinklerSimilarity.apply(null, "foo", UnicodeCharacterMatchFunctions.EQUAL));
        assertEquals(1.0, UnicodeJaroWinklerSimilarity.apply("", "", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(1.0, UnicodeJaroWinklerSimilarity.apply("foo", "foo", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.94, UnicodeJaroWinklerSimilarity.apply("foo", "foo ", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.91, UnicodeJaroWinklerSimilarity.apply("foo", "foo  ", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.87, UnicodeJaroWinklerSimilarity.apply("foo", " foo ", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.51, UnicodeJaroWinklerSimilarity.apply("foo", "  foo", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.0, UnicodeJaroWinklerSimilarity.apply("", "a", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.0, UnicodeJaroWinklerSimilarity.apply("aaapppp", "", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.93, UnicodeJaroWinklerSimilarity.apply("frog", "fog", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.0, UnicodeJaroWinklerSimilarity.apply("fly", "ant", UnicodeCharacterMatchFunctions.EQUAL));
        assertEquals(0.44, UnicodeJaroWinklerSimilarity.apply("elephant", "hippo", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.44, UnicodeJaroWinklerSimilarity.apply("hippo", "elephant", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.0, UnicodeJaroWinklerSimilarity.apply("hippo", "zzzzzzzz", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.88, UnicodeJaroWinklerSimilarity.apply("hello", "hallo", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.91, UnicodeJaroWinklerSimilarity.apply("ABC Corporation", "ABC Corp", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.95, UnicodeJaroWinklerSimilarity.apply("D N H Enterprises Inc", "D & H Enterprises, Inc.", UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.94, UnicodeJaroWinklerSimilarity.apply("My Gym Children's Fitness Center", "My Gym. Childrens Fitness",
                UnicodeCharacterMatchFunctions.EQUAL), delta);
        assertEquals(0.89, UnicodeJaroWinklerSimilarity.apply("PENNSYLVANIA", "PENNCISYLVNIA", UnicodeCharacterMatchFunctions.EQUAL), delta);
    }

    @Test
    void testHomoglyphSimilarity() {
        var measure = new JaroWinklerMeasure();
        assertEquals(1d, measure.getSimilarity(new ComparisonContext("ℜ𝘂ᖯʏ", "Ruby", UnicodeCharacterMatchFunctions.EQUAL_OR_HOMOGLYPH)), delta);
    }
}
