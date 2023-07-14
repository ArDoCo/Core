package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WordSimUtilsTest {

    @Test
    void getSimilarity() {
        assertEquals(WordSimUtils.getSimilarity("", ""), 1);
        assertEquals(WordSimUtils.getSimilarity("lorem", "lorem"), 1);
        assertEquals(WordSimUtils.getSimilarity("lorem ipsum", "lorem ipsum"), 1);
        assertEquals(WordSimUtils.getSimilarity("lOrEm IpSuM", "lorem ipsum", true), 1);
    }
}
