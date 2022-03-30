package edu.kit.kastel.mcse.ardoco.core.util;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;

class CommonUtilitiesTest {

    @Test
    void valueEqualTest() {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(CommonUtilities.valueEqual(1.0, 1.0)), //
                () -> Assertions.assertTrue(CommonUtilities.valueEqual(-1.0, -1.0)), //
                () -> Assertions.assertTrue(CommonUtilities.valueEqual(0.01, .01)), //
                () -> Assertions.assertTrue(CommonUtilities.valueEqual(0.1, 0.1 + 1e-9)), //
                () -> Assertions.assertTrue(CommonUtilities.valueEqual(0.1, 0.1 - 1e-9)), //
                () -> Assertions.assertFalse(CommonUtilities.valueEqual(1.0, 2.0)), //
                () -> Assertions.assertFalse(CommonUtilities.valueEqual(.1, -.1)), //
                () -> Assertions.assertFalse(CommonUtilities.valueEqual(0.1, 0.1 + 1e-7)), //
                () -> Assertions.assertFalse(CommonUtilities.valueEqual(0.1, 0.1 - 1e-7)) //
        );
    }

    @Test
    void harmonicMeanTest() {
        Assertions.assertAll(//
                () -> Assertions.assertEquals(0.5, CommonUtilities.harmonicMean(0.5, 0.5), 1e-8), //
                () -> Assertions.assertEquals(0, CommonUtilities.harmonicMean(0, 1), 1e-8), //
                () -> Assertions.assertEquals(0, CommonUtilities.harmonicMean(1, 0), 1e-8), //
                () -> Assertions.assertEquals(0.32, CommonUtilities.harmonicMean(0.2, 0.8), 1e-8), //
                () -> Assertions.assertEquals(0.32, CommonUtilities.harmonicMean(0.8, 0.2), 1e-8) //
        );
    }

    @Test
    void harmonicMeanListTest() {
        Assertions.assertAll(//
                () -> Assertions.assertEquals(0.5, CommonUtilities.harmonicMean(List.of(0.5, 0.5)), 1e-8), //
                () -> Assertions.assertEquals(0, CommonUtilities.harmonicMean(List.of(0d, 1d)), 1e-8), //
                () -> Assertions.assertEquals(0, CommonUtilities.harmonicMean(List.of(1d, 0d)), 1e-8), //
                () -> Assertions.assertEquals(0.32, CommonUtilities.harmonicMean(List.of(0.2, 0.8)), 1e-8), //
                () -> Assertions.assertEquals(0.32, CommonUtilities.harmonicMean(List.of(0.8, 0.2)), 1e-8), //
                () -> Assertions.assertEquals(0.5, CommonUtilities.harmonicMean(List.of(0.5, 0.5, 0.5)), 1e-8), //
                () -> Assertions.assertEquals(0, CommonUtilities.harmonicMean(List.of(0d, 0d, 1d)), 1e-8), //
                () -> Assertions.assertEquals(0, CommonUtilities.harmonicMean(List.of(1d, 0d, 0d)), 1e-8), //
                () -> Assertions.assertEquals(0.4, CommonUtilities.harmonicMean(List.of(0.2, 0.8, 0.8)), 1e-8), //
                () -> Assertions.assertEquals(0.4, CommonUtilities.harmonicMean(List.of(0.8, 0.8, 0.2)), 1e-8), //
                () -> Assertions.assertEquals(0.44366197183098594, CommonUtilities.harmonicMean(List.of(0.3, 0.5, 0.7)), 1e-8) //
        );
    }

    @Test
    void rootMeanSquareTest() {
        Assertions.assertAll(//
                () -> Assertions.assertEquals(0.5, CommonUtilities.rootMeanSquare(0.5, 0.5), 1e-8), //
                () -> Assertions.assertEquals(0.7071067811, CommonUtilities.rootMeanSquare(0, 1), 1e-8), //
                () -> Assertions.assertEquals(0.7071067811, CommonUtilities.rootMeanSquare(1, 0), 1e-8), //
                () -> Assertions.assertEquals(0.5830951894845301, CommonUtilities.rootMeanSquare(0.2, 0.8), 1e-8), //
                () -> Assertions.assertEquals(0.5830951894845301, CommonUtilities.rootMeanSquare(0.8, 0.2), 1e-8) //
        );
    }

    @Test
    void rootMeanSquareListTest() {
        Assertions.assertAll(//
                () -> Assertions.assertEquals(0.5, CommonUtilities.rootMeanSquare(List.of(0.5, 0.5)), 1e-8), //
                () -> Assertions.assertEquals(0.7071067811, CommonUtilities.rootMeanSquare(List.of(0d, 1d)), 1e-8), //
                () -> Assertions.assertEquals(0.7071067811, CommonUtilities.rootMeanSquare(List.of(1d, 0d)), 1e-8), //
                () -> Assertions.assertEquals(0.5830951894845301, CommonUtilities.rootMeanSquare(List.of(0.2, 0.8)), 1e-8), //
                () -> Assertions.assertEquals(0.5830951894845301, CommonUtilities.rootMeanSquare(List.of(0.8, 0.2)), 1e-8), //
                () -> Assertions.assertEquals(0.5, CommonUtilities.rootMeanSquare(List.of(0.5, 0.5, 0.5)), 1e-8), //
                () -> Assertions.assertEquals(0.5773502691896257, CommonUtilities.rootMeanSquare(List.of(0d, 0d, 1d)), 1e-8), //
                () -> Assertions.assertEquals(0.5773502691896257, CommonUtilities.rootMeanSquare(List.of(1d, 0d, 0d)), 1e-8), //
                () -> Assertions.assertEquals(0.66332495807108, CommonUtilities.rootMeanSquare(List.of(0.2, 0.8, 0.8)), 1e-8), //
                () -> Assertions.assertEquals(0.66332495807108, CommonUtilities.rootMeanSquare(List.of(0.8, 0.8, 0.2)), 1e-8), //
                () -> Assertions.assertEquals(0.5259911279353167, CommonUtilities.rootMeanSquare(List.of(0.3, 0.5, 0.7)), 1e-8) //
        );
    }

    @Test
    void cubicMeanTest() {
        Assertions.assertAll(//
                () -> Assertions.assertEquals(0.5, CommonUtilities.cubicMean(0.5, 0.5), 1e-8), //
                () -> Assertions.assertEquals(0.7937005259841, CommonUtilities.cubicMean(0, 1), 1e-8), //
                () -> Assertions.assertEquals(0.7937005259841, CommonUtilities.cubicMean(1, 0), 1e-8), //
                () -> Assertions.assertEquals(0.6382504298859908, CommonUtilities.cubicMean(0.2, 0.8), 1e-8), //
                () -> Assertions.assertEquals(0.6382504298859908, CommonUtilities.cubicMean(0.8, 0.2), 1e-8) //
        );
    }

    @Test
    void cubicMeanListTest() {
        Assertions.assertAll(//
                () -> Assertions.assertEquals(0.5, CommonUtilities.cubicMean(List.of(0.5, 0.5)), 1e-8), //
                () -> Assertions.assertEquals(0.7937005259841, CommonUtilities.cubicMean(List.of(0d, 1d)), 1e-8), //
                () -> Assertions.assertEquals(0.7937005259841, CommonUtilities.cubicMean(List.of(1d, 0d)), 1e-8), //
                () -> Assertions.assertEquals(0.6382504298859908, CommonUtilities.cubicMean(List.of(0.2, 0.8)), 1e-8), //
                () -> Assertions.assertEquals(0.6382504298859908, CommonUtilities.cubicMean(List.of(0.8, 0.2)), 1e-8), //
                () -> Assertions.assertEquals(0.5, CommonUtilities.cubicMean(List.of(0.5, 0.5, 0.5)), 1e-8), //
                () -> Assertions.assertEquals(0.6933612743506347, CommonUtilities.cubicMean(List.of(0d, 0d, 1d)), 1e-8), //
                () -> Assertions.assertEquals(0.6933612743506347, CommonUtilities.cubicMean(List.of(1d, 0d, 0d)), 1e-8), //
                () -> Assertions.assertEquals(0.7006796120773449, CommonUtilities.cubicMean(List.of(0.2, 0.8, 0.8)), 1e-8), //
                () -> Assertions.assertEquals(0.7006796120773449, CommonUtilities.cubicMean(List.of(0.8, 0.8, 0.2)), 1e-8), //
                () -> Assertions.assertEquals(0.5484806552432618, CommonUtilities.cubicMean(List.of(0.3, 0.5, 0.7)), 1e-8) //
        );
    }

    @Test
    void powerMeanTest() {
        Assertions.assertAll(//
                () -> Assertions.assertEquals(0.5, CommonUtilities.powerMean(0.5, 0.5, 2), 1e-8), //
                () -> Assertions.assertEquals(0.7071067811, CommonUtilities.powerMean(0, 1, 2), 1e-8), //
                () -> Assertions.assertEquals(0.7071067811, CommonUtilities.powerMean(1, 0, 2), 1e-8), //
                () -> Assertions.assertEquals(0.5830951894845301, CommonUtilities.powerMean(0.2, 0.8, 2), 1e-8), //
                () -> Assertions.assertEquals(0.5830951894845301, CommonUtilities.powerMean(0.8, 0.2, 2), 1e-8), //
                () -> Assertions.assertEquals(0.5, CommonUtilities.powerMean(0.5, 0.5, 3), 1e-8), //
                () -> Assertions.assertEquals(0.7937005259841, CommonUtilities.powerMean(0, 1, 3), 1e-8), //
                () -> Assertions.assertEquals(0.7937005259841, CommonUtilities.powerMean(1, 0, 3), 1e-8), //
                () -> Assertions.assertEquals(0.6382504298859908, CommonUtilities.powerMean(0.2, 0.8, 3), 1e-8), //
                () -> Assertions.assertEquals(0.6382504298859908, CommonUtilities.powerMean(0.8, 0.2, 3), 1e-8) //

        );
    }

    @Test
    void powerMeanListTest() {
        Assertions.assertAll(//
                () -> Assertions.assertEquals(0.5, CommonUtilities.powerMean(List.of(0.5, 0.5), 2), 1e-8), //
                () -> Assertions.assertEquals(0.7071067811, CommonUtilities.powerMean(List.of(0d, 1d), 2), 1e-8), //
                () -> Assertions.assertEquals(0.7071067811, CommonUtilities.powerMean(List.of(1d, 0d), 2), 1e-8), //
                () -> Assertions.assertEquals(0.5830951894845301, CommonUtilities.powerMean(List.of(0.2, 0.8), 2), 1e-8), //
                () -> Assertions.assertEquals(0.5830951894845301, CommonUtilities.powerMean(List.of(0.8, 0.2), 2), 1e-8), //
                () -> Assertions.assertEquals(0.5, CommonUtilities.powerMean(List.of(0.5, 0.5, 0.5), 2), 1e-8), //
                () -> Assertions.assertEquals(0.5773502691896257, CommonUtilities.powerMean(List.of(0d, 0d, 1d), 2), 1e-8), //
                () -> Assertions.assertEquals(0.5773502691896257, CommonUtilities.powerMean(List.of(1d, 0d, 0d), 2), 1e-8), //
                () -> Assertions.assertEquals(0.66332495807108, CommonUtilities.powerMean(List.of(0.2, 0.8, 0.8), 2), 1e-8), //
                () -> Assertions.assertEquals(0.66332495807108, CommonUtilities.powerMean(List.of(0.8, 0.8, 0.2), 2), 1e-8), //
                () -> Assertions.assertEquals(0.5259911279353167, CommonUtilities.powerMean(List.of(0.3, 0.5, 0.7), 2), 1e-8), //
                () -> Assertions.assertEquals(0.5, CommonUtilities.powerMean(List.of(0.5, 0.5), 3), 1e-8), //
                () -> Assertions.assertEquals(0.7937005259841, CommonUtilities.powerMean(List.of(0d, 1d), 3), 1e-8), //
                () -> Assertions.assertEquals(0.7937005259841, CommonUtilities.powerMean(List.of(1d, 0d), 3), 1e-8), //
                () -> Assertions.assertEquals(0.6382504298859908, CommonUtilities.powerMean(List.of(0.2, 0.8), 3), 1e-8), //
                () -> Assertions.assertEquals(0.6382504298859908, CommonUtilities.powerMean(List.of(0.8, 0.2), 3), 1e-8), //
                () -> Assertions.assertEquals(0.5, CommonUtilities.powerMean(List.of(0.5, 0.5, 0.5), 3), 1e-8), //
                () -> Assertions.assertEquals(0.6933612743506347, CommonUtilities.powerMean(List.of(0d, 0d, 1d), 3), 1e-8), //
                () -> Assertions.assertEquals(0.6933612743506347, CommonUtilities.powerMean(List.of(1d, 0d, 0d), 3), 1e-8), //
                () -> Assertions.assertEquals(0.7006796120773449, CommonUtilities.powerMean(List.of(0.2, 0.8, 0.8), 3), 1e-8), //
                () -> Assertions.assertEquals(0.7006796120773449, CommonUtilities.powerMean(List.of(0.8, 0.8, 0.2), 3), 1e-8), //
                () -> Assertions.assertEquals(0.5484806552432618, CommonUtilities.powerMean(List.of(0.3, 0.5, 0.7), 3), 1e-8) //
        );
    }
}
