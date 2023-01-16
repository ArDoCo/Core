/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResultsImpl;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExtendedEvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExtendedEvaluationResultsImpl;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ResultCalculator;

class ResultCalculatorTest {

    @DisplayName("Weighted Average Results With EvaluationResults and equal weights")
    @Test
    void weightedAverageResultsWithEvaluationResults_equalWeights() {
        ResultCalculator rc = new ResultCalculator();
        rc.addEvaluationResults(new EvaluationResultsImpl(1.0, 1.0, 1.0), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.0, 1.0, 0.0), 1);

        var results = rc.getWeightedAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(results.getClass().getSimpleName(), EvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.5, results.getPrecision(), 1e-5, "Unexpected Precision"), //
                () -> Assertions.assertEquals(1.0, results.getRecall(), 1e-5, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.5, results.getF1(), 1e-5, "Unexpected F1")//
        );

        rc.addEvaluationResults(new EvaluationResultsImpl(.9, .9, .9), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.9, 0.1, 0.18), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.6, 0.4, 0.48), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.3, 0.7, 0.42), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.6, 0.4, 0.48), 1);

        var newResults = rc.getWeightedAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(newResults.getClass().getSimpleName(), EvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.614285714, newResults.getPrecision(), 1e-5, "Unexpected Precision"), //
                () -> Assertions.assertEquals(0.642857143, newResults.getRecall(), 1e-5, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.494285714, newResults.getF1(), 1e-5, "Unexpected F1")//
        );
    }

    @DisplayName("Weighted Average Results With EvaluationResults and unequal weights")
    @Test
    void weightedAverageResultsWithEvaluationResults_unequalWeights() {
        ResultCalculator rc = new ResultCalculator();
        rc.addEvaluationResults(new EvaluationResultsImpl(1.0, 1.0, 1.0), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.0, 1.0, 0.0), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(.9, .9, .9), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.9, 0.1, 0.18), 2);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.6, 0.4, 0.48), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.3, 0.7, 0.42), 2);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.6, 0.4, 0.48), 2);

        var results = rc.getWeightedAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(results.getClass().getSimpleName(), EvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.61, results.getPrecision(), 1e-5, "Unexpected Precision"), //
                () -> Assertions.assertEquals(0.57, results.getRecall(), 1e-5, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.454, results.getF1(), 1e-5, "Unexpected F1")//
        );
    }

    @DisplayName("Macro Average Results With EvaluationResults")
    @Test
    void macroAverageResultsWithEvaluationResults() {
        ResultCalculator rc = new ResultCalculator();
        rc.addEvaluationResults(new EvaluationResultsImpl(1.0, 1.0, 1.0), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.0, 1.0, 0.0), 1);

        var results = rc.getMacroAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(results.getClass().getSimpleName(), EvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.5, results.getPrecision(), 1e-5, "Unexpected Precision"), //
                () -> Assertions.assertEquals(1.0, results.getRecall(), 1e-5, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.5, results.getF1(), 1e-5, "Unexpected F1")//
        );

        rc.addEvaluationResults(new EvaluationResultsImpl(.9, .9, .9), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.9, 0.1, 0.18), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.6, 0.4, 0.48), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.3, 0.7, 0.42), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.6, 0.4, 0.48), 1);

        var newResults = rc.getWeightedAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(newResults.getClass().getSimpleName(), EvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.614285714, newResults.getPrecision(), 1e-5, "Unexpected Precision"), //
                () -> Assertions.assertEquals(0.642857143, newResults.getRecall(), 1e-5, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.494285714, newResults.getF1(), 1e-5, "Unexpected F1")//
        );
    }

    @DisplayName("Weighted Average Results With ExtendedEvaluationResults and equal weights")
    @Test
    void weightedAverageResultsWithExtendedEvaluationResults_equalWeights() {
        ResultCalculator rc = new ResultCalculator();
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(1.0, 1.0, 1.0, 1.0, 1.0, 1.0), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.0, 1.0, 0.0, 0.0, 0.0, 0.0), 1);

        var results = (ExtendedEvaluationResults) rc.getWeightedAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(results.getClass().getSimpleName(), ExtendedEvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.5, results.getPrecision(), 1e-5, "Unexpected Precision"), //
                () -> Assertions.assertEquals(1.0, results.getRecall(), 1e-5, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.5, results.getF1(), 1e-5, "Unexpected F1"), //
                () -> Assertions.assertEquals(0.5, results.getAccuracy(), 1e-5, "Unexpected Accuracy"), //
                () -> Assertions.assertEquals(0.5, results.getPhiCoefficient(), 1e-5, "Unexpected Phi Coefficient"), //
                () -> Assertions.assertEquals(0.5, results.getSpecificity(), 1e-5, "Unexpected Specificity") //
        );

        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.9, 0.1, 0.18, 0.6, 0.1, 0.1), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.6, 0.4, 0.48, 0.7, 0.3, 0.3), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.3, 0.7, 0.42, 0.5, 0.45, 0.45), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.9, 0.9, 0.9, 0.4, 0.6, 0.6), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.6, 0.4, 0.48, 0.3, 0.35, 0.35), 1);

        var newResults = (ExtendedEvaluationResults) rc.getWeightedAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(newResults.getClass().getSimpleName(), ExtendedEvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.614285714, newResults.getPrecision(), 1e-5, "Unexpected Precision"), //
                () -> Assertions.assertEquals(0.642857143, newResults.getRecall(), 1e-5, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.494285714, newResults.getF1(), 1e-5, "Unexpected F1"), //
                () -> Assertions.assertEquals(0.5, newResults.getAccuracy(), 1e-5, "Unexpected Accuracy"), //
                () -> Assertions.assertEquals(0.4, newResults.getPhiCoefficient(), 1e-5, "Unexpected Phi Coefficient"), //
                () -> Assertions.assertEquals(0.4, newResults.getSpecificity(), 1e-5, "Unexpected Specificity") //
        );
    }

    @DisplayName("Weighted Average Results With ExtendedEvaluationResults and unequal weights")
    @Test
    void weightedAverageResultsWithExtendedEvaluationResults_unequalWeights() {
        ResultCalculator rc = new ResultCalculator();
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(1.0, 1.0, 1.0, 1.0, 1.0, 1.0), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.0, 1.0, 0.0, 0.0, 0.0, 0.0), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.9, 0.1, 0.18, 0.6, 0.1, 0.1), 2);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.6, 0.4, 0.48, 0.7, 0.3, 0.3), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.3, 0.7, 0.42, 0.5, 0.45, 0.45), 2);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.9, 0.9, 0.9, 0.4, 0.6, 0.6), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.6, 0.4, 0.48, 0.3, 0.35, 0.35), 2);

        var results = (ExtendedEvaluationResults) rc.getWeightedAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(results.getClass().getSimpleName(), ExtendedEvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.61, results.getPrecision(), 1e-5, "Unexpected Precision"), //
                () -> Assertions.assertEquals(0.57, results.getRecall(), 1e-5, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.454, results.getF1(), 1e-5, "Unexpected F1"), //
                () -> Assertions.assertEquals(0.49, results.getAccuracy(), 1e-5, "Unexpected Accuracy"), //
                () -> Assertions.assertEquals(0.37, results.getPhiCoefficient(), 1e-5, "Unexpected Phi Coefficient"), //
                () -> Assertions.assertEquals(0.37, results.getSpecificity(), 1e-5, "Unexpected Specificity") //
        );
    }

    @DisplayName("Macro Average Results With ExtendedEvaluationResults")
    @Test
    void macroAverageResultsWithExtendedEvaluationResults() {
        ResultCalculator rc = new ResultCalculator();
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(1.0, 1.0, 1.0, 1.0, 1.0, 1.0), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.0, 1.0, 0.0, 0.0, 0.0, 0.0), 1);

        var results = (ExtendedEvaluationResults) rc.getMacroAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(results.getClass().getSimpleName(), ExtendedEvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.5, results.getPrecision(), 1e-5, "Unexpected Precision"), //
                () -> Assertions.assertEquals(1.0, results.getRecall(), 1e-5, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.5, results.getF1(), 1e-5, "Unexpected F1"), //
                () -> Assertions.assertEquals(0.5, results.getAccuracy(), 1e-5, "Unexpected Accuracy"), //
                () -> Assertions.assertEquals(0.5, results.getPhiCoefficient(), 1e-5, "Unexpected Phi Coefficient"), //
                () -> Assertions.assertEquals(0.5, results.getSpecificity(), 1e-5, "Unexpected Specificity") //
        );

        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.9, 0.1, 0.18, 0.6, 0.1, 0.1), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.6, 0.4, 0.48, 0.7, 0.3, 0.3), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.3, 0.7, 0.42, 0.5, 0.45, 0.45), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.9, 0.9, 0.9, 0.4, 0.6, 0.6), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.6, 0.4, 0.48, 0.3, 0.35, 0.35), 1);

        var newResults = (ExtendedEvaluationResults) rc.getMacroAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(newResults.getClass().getSimpleName(), ExtendedEvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.614285714, newResults.getPrecision(), 1e-5, "Unexpected Precision"), //
                () -> Assertions.assertEquals(0.642857143, newResults.getRecall(), 1e-5, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.494285714, newResults.getF1(), 1e-5, "Unexpected F1"), //
                () -> Assertions.assertEquals(0.5, newResults.getAccuracy(), 1e-5, "Unexpected Accuracy"), //
                () -> Assertions.assertEquals(0.4, newResults.getPhiCoefficient(), 1e-5, "Unexpected Phi Coefficient"), //
                () -> Assertions.assertEquals(0.4, newResults.getSpecificity(), 1e-5, "Unexpected Specificity") //
        );
    }

}
