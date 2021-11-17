package edu.kit.kastel.mcse.ardoco.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

class SimilarityUtilsTest {

    @Test
    void areWordsSimilarEqualTest() {
        boolean similar = SimilarityUtils.areWordsSimilar("one", "one");
        Assertions.assertTrue(similar);

        similar = SimilarityUtils.areWordsSimilar("", "");
        Assertions.assertTrue(similar);

        similar = SimilarityUtils.areWordsSimilar("testtesttesttest", "testtesttesttest");
        Assertions.assertTrue(similar);
    }

    @Test
    void areWordsSimilarSimilarTest() {
        boolean similar = SimilarityUtils.areWordsSimilar("test", "tast");
        Assertions.assertTrue(similar);

        similar = SimilarityUtils.areWordsSimilar("testtesttesttest", "testtesttesttes");
        Assertions.assertTrue(similar);
    }

    @Test
    void areWordsSimilarUnsimilarTest() {
        boolean similar = SimilarityUtils.areWordsSimilar("test", "toast");
        Assertions.assertFalse(similar);

        similar = SimilarityUtils.areWordsSimilar("test", "tset");
        Assertions.assertFalse(similar);

        similar = SimilarityUtils.areWordsSimilar("testtesttesttest", "testtesttest");
        Assertions.assertFalse(similar);
    }
}
