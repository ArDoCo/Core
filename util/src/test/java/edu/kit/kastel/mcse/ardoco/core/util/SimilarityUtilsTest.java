/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

class SimilarityUtilsTest {

    @Test
    void areWordsSimilarEqualTest() {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(SimilarityUtils.areWordsSimilar("one", "one")),
                () -> Assertions.assertTrue(SimilarityUtils.areWordsSimilar("", "")),
                () -> Assertions.assertTrue(SimilarityUtils.areWordsSimilar("testtesttesttest", "testtesttesttest")));
    }

    @Test
    void areWordsSimilarSimilarTest() {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(SimilarityUtils.areWordsSimilar("test", "tast")),
                () -> Assertions.assertTrue(SimilarityUtils.areWordsSimilar("testtesttesttest", "testtesttesttes")));
    }

    @Test
    void areWordsSimilarUnsimilarTest() {
        Assertions.assertAll(//
                () -> Assertions.assertFalse(SimilarityUtils.areWordsSimilar("test", "toast")),
                () -> Assertions.assertFalse(SimilarityUtils.areWordsSimilar("test", "coast")),
                () -> Assertions.assertFalse(SimilarityUtils.areWordsSimilar("testtesttesttest", "testertest")));
    }

    @Test
    void areWordsSimilarAlmostSimilarTest() {
        Assertions.assertAll(//
                () -> Assertions.assertFalse(SimilarityUtils.areWordsSimilar("management", "mediamanagement")),
                () -> Assertions.assertFalse(SimilarityUtils.areWordsSimilar("management", "usermanagement")),
                () -> Assertions.assertFalse(SimilarityUtils.areWordsSimilar("mediamanagement", "usermanagement")),
                () -> Assertions.assertFalse(SimilarityUtils.areWordsSimilar("image", "page")));
    }
}
