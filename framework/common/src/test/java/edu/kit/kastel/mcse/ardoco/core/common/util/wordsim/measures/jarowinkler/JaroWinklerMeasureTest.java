package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class JaroWinklerMeasureTest {
    private static final double delta = 0.01;

    /**
     * These tests were extracted from the original {@link org.apache.commons.text.similarity.JaroWinklerSimilarity} implementation and should
     * still hold true.
     */
    @Test
    void testSimilarityDirectly() {
        String s = null;
        assertThrows(IllegalArgumentException.class, () -> UnicodeJaroWinklerSimilarity.apply(s, s, UnicodeCharacter.EQUAL));
        assertThrows(IllegalArgumentException.class, () -> UnicodeJaroWinklerSimilarity.apply("foo", null, UnicodeCharacter.EQUAL));
        assertThrows(IllegalArgumentException.class, () -> UnicodeJaroWinklerSimilarity.apply(null, "foo", UnicodeCharacter.EQUAL));
        assertEquals(1.0, UnicodeJaroWinklerSimilarity.apply("", "", UnicodeCharacter.EQUAL), delta);
        assertEquals(1.0, UnicodeJaroWinklerSimilarity.apply("foo", "foo", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.94, UnicodeJaroWinklerSimilarity.apply("foo", "foo ", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.91, UnicodeJaroWinklerSimilarity.apply("foo", "foo  ", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.87, UnicodeJaroWinklerSimilarity.apply("foo", " foo ", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.51, UnicodeJaroWinklerSimilarity.apply("foo", "  foo", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.0, UnicodeJaroWinklerSimilarity.apply("", "a", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.0, UnicodeJaroWinklerSimilarity.apply("aaapppp", "", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.93, UnicodeJaroWinklerSimilarity.apply("frog", "fog", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.0, UnicodeJaroWinklerSimilarity.apply("fly", "ant", UnicodeCharacter.EQUAL));
        assertEquals(0.44, UnicodeJaroWinklerSimilarity.apply("elephant", "hippo", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.44, UnicodeJaroWinklerSimilarity.apply("hippo", "elephant", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.0, UnicodeJaroWinklerSimilarity.apply("hippo", "zzzzzzzz", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.88, UnicodeJaroWinklerSimilarity.apply("hello", "hallo", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.91, UnicodeJaroWinklerSimilarity.apply("ABC Corporation", "ABC Corp", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.95, UnicodeJaroWinklerSimilarity.apply("D N H Enterprises Inc", "D & H Enterprises, Inc.", UnicodeCharacter.EQUAL), delta);
        assertEquals(0.94, UnicodeJaroWinklerSimilarity.apply("My Gym Children's Fitness Center", "My Gym. Childrens Fitness",
                UnicodeCharacter.EQUAL), delta);
        assertEquals(0.89, UnicodeJaroWinklerSimilarity.apply("PENNSYLVANIA", "PENNCISYLVNIA", UnicodeCharacter.EQUAL), delta);
    }

    @Test
    void testHomoglyphSimilarity() {
        var measure = new JaroWinklerMeasure();
        assertEquals(1d, measure.getSimilarity(new ComparisonContext("ℜ𝘂ᖯʏ", "Ruby", UnicodeCharacter.EQUAL_OR_HOMOGLYPH)),
                delta);
    }
}
