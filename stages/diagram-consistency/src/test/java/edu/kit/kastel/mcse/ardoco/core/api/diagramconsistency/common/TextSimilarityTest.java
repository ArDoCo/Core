package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.TextSimilarity;

class TextSimilarityTest {
    @Test
    void byLevenshtein() {
        assertEquals(1.0, TextSimilarity.byLevenshtein("a", "a"));
        assertEquals(0.0, TextSimilarity.byLevenshtein("x", "y"));
    }

    @Test
    void byJaccard() {
        assertEquals(1.0, TextSimilarity.byJaccard("a", "a"));
        assertEquals(0.0, TextSimilarity.byJaccard("x", "y"));
    }
}
