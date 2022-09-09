package edu.kit.kastel.mcse.ardoco.core.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestUtilTest {

    @Test
    void calculatePrecisionTest() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(.5, TestUtil.calculatePrecision(10, 10), 1e-3), //
                () -> Assertions.assertEquals(.857, TestUtil.calculatePrecision(6, 1), 1e-3), //
                () -> Assertions.assertEquals(.154, TestUtil.calculatePrecision(10, 55), 1e-3), //
                () -> Assertions.assertEquals(.905, TestUtil.calculatePrecision(210, 22), 1e-3) //
        );
    }

    @Test
    void calculateRecallTest() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(.5, TestUtil.calculateRecall(10, 10), 1e-3), //
                () -> Assertions.assertEquals(.75, TestUtil.calculateRecall(6, 2), 1e-3), //
                () -> Assertions.assertEquals(.154, TestUtil.calculateRecall(10, 55), 1e-3), //
                () -> Assertions.assertEquals(.871, TestUtil.calculateRecall(210, 31), 1e-3) //
        );
    }

    @Test
    void calculateF1FromPrecisionRecallTest() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(1.0, TestUtil.calculateF1(1., 1.), 1e-2), //
                () -> Assertions.assertEquals(0.0, TestUtil.calculateF1(0., 1.), 1e-2), //
                () -> Assertions.assertEquals(0.0, TestUtil.calculateF1(1., 0.), 1e-2), //
                () -> Assertions.assertEquals(0.18, TestUtil.calculateF1(.9, .1), 1e-2), //
                () -> Assertions.assertEquals(0.48, TestUtil.calculateF1(.6, .4), 1e-2), //
                () -> Assertions.assertEquals(0.42, TestUtil.calculateF1(.3, .7), 1e-2), //
                () -> Assertions.assertEquals(0.9, TestUtil.calculateF1(.9, .9), 1e-2), //
                () -> Assertions.assertEquals(0.48, TestUtil.calculateF1(.4, .6), 1e-2) //
        );
    }

    @Test
    void calculateF1Test() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(.5, TestUtil.calculateF1(10, 10, 10), 1e-3), //
                () -> Assertions.assertEquals(.8, TestUtil.calculateF1(6, 1, 2), 1e-3), //
                () -> Assertions.assertEquals(.154, TestUtil.calculateF1(10, 55, 55), 1e-3), //
                () -> Assertions.assertEquals(.888, TestUtil.calculateF1(210, 22, 31), 1e-3) //
        );
    }

    @Test
    void calculateAccuracyTest() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(.5, TestUtil.calculateAccuracy(10, 10, 10, 10), 1e-3), //
                () -> Assertions.assertEquals(.75, TestUtil.calculateAccuracy(6, 1, 2, 3), 1e-3), //
                () -> Assertions.assertEquals(.214, TestUtil.calculateAccuracy(10, 55, 55, 20), 1e-3), //
                () -> Assertions.assertEquals(.967, TestUtil.calculateAccuracy(210, 22, 31, 1337), 1e-3) //
        );
    }

    @Test
    void calculatePhiCoefficientTest() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(.0, TestUtil.calculatePhiCoefficient(10, 10, 10, 10), 1e-3), //
                () -> Assertions.assertEquals(.478, TestUtil.calculatePhiCoefficient(6, 1, 2, 3), 1e-3), //
                () -> Assertions.assertEquals(-.579, TestUtil.calculatePhiCoefficient(10, 55, 55, 20), 1e-3), //
                () -> Assertions.assertEquals(.869, TestUtil.calculatePhiCoefficient(210, 22, 31, 1337), 1e-3), //
                () -> Assertions.assertEquals(.0, TestUtil.calculatePhiCoefficient(0, 0, 11, 11), 1e-3), //
                () -> Assertions.assertEquals(.0, TestUtil.calculatePhiCoefficient(11, 0, 11, 0), 1e-3) //
        );
    }

}
