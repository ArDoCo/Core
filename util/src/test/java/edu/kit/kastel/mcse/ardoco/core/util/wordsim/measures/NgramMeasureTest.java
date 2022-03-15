/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.util.wordsim.measures;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.ngram.NgramMeasure;
import edu.kit.kastel.mcse.ardoco.core.util.wordsim.TestUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link NgramMeasure}.
 */
public class NgramMeasureTest {

    // TODO: Check if all these floating point equality assertions actually work as intended

    @Test
    public void testUnigramDistance() {
        var measure = new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 1, 1.0);
        var levenshteinDistance = new LevenshteinDistance();

        assertEquals(1.0, measure.calculateDistance("Hello", "Hella"));

        for (String firstWord : TestUtils.RANDOM_WORDS) {
            for (String secondWord : TestUtils.RANDOM_WORDS) {
                double levenshteinResult = levenshteinDistance.apply(firstWord, secondWord);
                double ngramResult = measure.calculateDistance(firstWord, secondWord);

                assertEquals(levenshteinResult, ngramResult);
            }
        }

        assertEquals(0.0, measure.calculateDistance("", ""));
        assertEquals("Hello".length(), measure.calculateDistance("Hello", ""));
        assertEquals("Hello".length(), measure.calculateDistance("", "Hello"));
    }

    @Test
    public void testUnigramSimilarity() {
        var measure = new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 1, 0.7);

        assertTrue(measure.areWordsSimilar(new ComparisonContext("Hello", "Hella"))); // 0.8
        assertFalse(measure.areWordsSimilar(new ComparisonContext("Hello", "Heal"))); // 0.6
    }

    @Test
    public void testBigram() {
        var measure = new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 2, 0.7);
        assertEquals(0.5, measure.calculateDistance("Hello", "Hella"));

        assertTrue(measure.areWordsSimilar(new ComparisonContext("Hello", "Hella"))); // 0.8
        assertFalse(measure.areWordsSimilar(new ComparisonContext("Hello", "Heal"))); // 0.6

        assertEquals(0.0, measure.calculateDistance("", ""));
    }

    @Test
    public void testTrigram() {
        var measure = new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 3, 0.7);
        assertEquals(1.0 / 3.0, measure.calculateDistance("Hello", "Hella"));

        assertTrue(measure.areWordsSimilar(new ComparisonContext("Hello", "Hella"))); // 0.8
        assertFalse(measure.areWordsSimilar(new ComparisonContext("Hello", "Heal"))); // 0.6

        assertEquals(0.0, measure.calculateDistance("", ""));
    }

    @Test
    public void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new NgramMeasure(NgramMeasure.Variant.LUCENE, -1, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new NgramMeasure(NgramMeasure.Variant.LUCENE, 1, -0.1));
        assertThrows(IllegalArgumentException.class, () -> new NgramMeasure(NgramMeasure.Variant.LUCENE, 1, 1.1));
        assertThrows(NullPointerException.class, () -> new NgramMeasure(null, 1, 1.1));
        new NgramMeasure(NgramMeasure.Variant.LUCENE, 1, 0.0);
        new NgramMeasure(NgramMeasure.Variant.LUCENE, 1, 0.5);
        new NgramMeasure(NgramMeasure.Variant.LUCENE, 1, 1.0);
        new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 1, 1.0);
        new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 2, 1.0);
        new NgramMeasure(NgramMeasure.Variant.POSITIONAL, 12345, 1.0);
        new NgramMeasure(NgramMeasure.Variant.POSITIONAL, Integer.MAX_VALUE, 1.0);
    }

}
