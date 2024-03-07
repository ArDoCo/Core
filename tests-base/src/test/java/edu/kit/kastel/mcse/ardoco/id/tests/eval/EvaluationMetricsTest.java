/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.id.tests.eval;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EvaluationMetricsTest {

    @Test
    void calculatePrecisionTest() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(.5, EvaluationMetrics.calculatePrecision(10, 10), 1e-3), //
                () -> Assertions.assertEquals(.857, EvaluationMetrics.calculatePrecision(6, 1), 1e-3), //
                () -> Assertions.assertEquals(.154, EvaluationMetrics.calculatePrecision(10, 55), 1e-3), //
                () -> Assertions.assertEquals(.905, EvaluationMetrics.calculatePrecision(210, 22), 1e-3) //
        );
    }

    @Test
    void calculateRecallTest() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(.5, EvaluationMetrics.calculateRecall(10, 10), 1e-3), //
                () -> Assertions.assertEquals(.75, EvaluationMetrics.calculateRecall(6, 2), 1e-3), //
                () -> Assertions.assertEquals(.154, EvaluationMetrics.calculateRecall(10, 55), 1e-3), //
                () -> Assertions.assertEquals(.871, EvaluationMetrics.calculateRecall(210, 31), 1e-3) //
        );
    }

    @Test
    void calculateF1FromPrecisionRecallTest() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(1.0, EvaluationMetrics.calculateF1(1., 1.), 1e-2), //
                () -> Assertions.assertEquals(0.0, EvaluationMetrics.calculateF1(0., 1.), 1e-2), //
                () -> Assertions.assertEquals(0.0, EvaluationMetrics.calculateF1(1., 0.), 1e-2), //
                () -> Assertions.assertEquals(0.18, EvaluationMetrics.calculateF1(.9, .1), 1e-2), //
                () -> Assertions.assertEquals(0.48, EvaluationMetrics.calculateF1(.6, .4), 1e-2), //
                () -> Assertions.assertEquals(0.42, EvaluationMetrics.calculateF1(.3, .7), 1e-2), //
                () -> Assertions.assertEquals(0.9, EvaluationMetrics.calculateF1(.9, .9), 1e-2), //
                () -> Assertions.assertEquals(0.48, EvaluationMetrics.calculateF1(.4, .6), 1e-2) //
        );
    }

    @Test
    void calculateF1Test() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(.5, EvaluationMetrics.calculateF1(10, 10, 10), 1e-3), //
                () -> Assertions.assertEquals(.8, EvaluationMetrics.calculateF1(6, 1, 2), 1e-3), //
                () -> Assertions.assertEquals(.154, EvaluationMetrics.calculateF1(10, 55, 55), 1e-3), //
                () -> Assertions.assertEquals(.888, EvaluationMetrics.calculateF1(210, 22, 31), 1e-3) //
        );
    }

    @Test
    void calculateAccuracyTest() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(.5, EvaluationMetrics.calculateAccuracy(10, 10, 10, 10), 1e-3), //
                () -> Assertions.assertEquals(.75, EvaluationMetrics.calculateAccuracy(6, 1, 2, 3), 1e-3), //
                () -> Assertions.assertEquals(.214, EvaluationMetrics.calculateAccuracy(10, 55, 55, 20), 1e-3), //
                () -> Assertions.assertEquals(.967, EvaluationMetrics.calculateAccuracy(210, 22, 31, 1337), 1e-3) //
        );
    }

    @Test
    void calculatePhiCoefficientTest() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(.0, EvaluationMetrics.calculatePhiCoefficient(10, 10, 10, 10), 1e-3), //
                () -> Assertions.assertEquals(.478, EvaluationMetrics.calculatePhiCoefficient(6, 1, 2, 3), 1e-3), //
                () -> Assertions.assertEquals(-.579, EvaluationMetrics.calculatePhiCoefficient(10, 55, 55, 20), 1e-3), //
                () -> Assertions.assertEquals(.869, EvaluationMetrics.calculatePhiCoefficient(210, 22, 31, 1337), 1e-3), //
                () -> Assertions.assertEquals(.0, EvaluationMetrics.calculatePhiCoefficient(0, 0, 11, 11), 1e-3), //
                () -> Assertions.assertEquals(.0, EvaluationMetrics.calculatePhiCoefficient(11, 0, 11, 0), 1e-3) //
        );
    }

    @Test
    void calculateSpecificityTest() {
        Assertions.assertAll( //
                () -> Assertions.assertEquals(.5, EvaluationMetrics.calculateSpecificity(1, 1), 1e-3), //
                () -> Assertions.assertEquals(.76, EvaluationMetrics.calculateSpecificity(1337, 420), 1e-3), //
                () -> Assertions.assertEquals(.0, EvaluationMetrics.calculateSpecificity(0, 20), 1e-3), //
                () -> Assertions.assertEquals(1., EvaluationMetrics.calculateSpecificity(20, 0), 1e-3), //
                () -> Assertions.assertEquals(1., EvaluationMetrics.calculateSpecificity(0, 0), 1e-3), //
                () -> Assertions.assertEquals(.375, EvaluationMetrics.calculateSpecificity(3, 5), 1e-3) //
        );
    }

}
