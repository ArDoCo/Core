package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WordSimUtilsTest {

    @Test
    void getSimilarity() {
        assertEquals(1, WordSimUtils.getSimilarity("", ""));
        assertEquals(1, WordSimUtils.getSimilarity("lorem", "lorem"));
        assertEquals(1, WordSimUtils.getSimilarity("lorem ipsum", "lorem ipsum"));
        assertEquals(1, WordSimUtils.getSimilarity("lOrEm IpSuM", "lorem ipsum", true));
    }
}
