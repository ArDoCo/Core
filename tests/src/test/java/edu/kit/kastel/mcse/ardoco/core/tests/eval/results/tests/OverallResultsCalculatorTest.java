/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResultsImpl;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExtendedEvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExtendedEvaluationResultsImpl;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.OverallResultsCalculator;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ResultCalculator;

class OverallResultsCalculatorTest {

    @DisplayName("Weighted Average Results With EvaluationResults and equal weights")
    @Test
    void weightedAverageResultsWithEvaluationResultsImpl_equalWeights() {
        OverallResultsCalculator orc = new OverallResultsCalculator();

        ResultCalculator rc = new ResultCalculator();
        rc.addEvaluationResults(new EvaluationResultsImpl(1.0, 1.0, 1.0), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.0, 1.0, 0.0), 1);
        orc.addResult(Project.MEDIASTORE, rc);

        ResultCalculator rc2 = new ResultCalculator();
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.9, 0.1, 0.18), 1);
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.6, 0.4, 0.48), 1);
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.3, 0.7, 0.42), 1);
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.9, 0.9, 0.9), 1);
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.6, 0.4, 0.48), 1);
        orc.addResult(Project.TEASTORE, rc2);

        var results = orc.calculateWeightedAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(results.getClass().getSimpleName(), EvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.614, results.getPrecision(), 1e-3, "Unexpected Precision"), //
                () -> Assertions.assertEquals(0.643, results.getRecall(), 1e-3, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.494, results.getF1(), 1e-3, "Unexpected F1")//
        );
    }

    @DisplayName("Weighted Average Results With EvaluationResults and unequal weights")
    @Test
    void weightedAverageResultsWithEvaluationResultsImpl_unequalWeights() {
        OverallResultsCalculator orc = new OverallResultsCalculator();

        ResultCalculator rc = new ResultCalculator();
        rc.addEvaluationResults(new EvaluationResultsImpl(1.0, 1.0, 1.0), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.0, 1.0, 0.0), 1);
        orc.addResult(Project.MEDIASTORE, rc);

        ResultCalculator rc2 = new ResultCalculator();
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.9, 0.1, 0.18), 2);
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.6, 0.4, 0.48), 1);
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.3, 0.7, 0.42), 2);
        rc2.addEvaluationResults(new EvaluationResultsImpl(.9, .9, .9), 1);
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.6, 0.4, 0.48), 2);
        orc.addResult(Project.TEASTORE, rc2);

        var results = orc.calculateWeightedAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(results.getClass().getSimpleName(), EvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.610, results.getPrecision(), 1e-3, "Unexpected Precision"), //
                () -> Assertions.assertEquals(0.570, results.getRecall(), 1e-3, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.454, results.getF1(), 1e-3, "Unexpected F1")//
        );
    }

    @DisplayName("Weighted Average Results With EvaluationResults and equal weights")
    @Test
    void macroAverageResultsWithEvaluationResultsImpl_equalWeights() {
        OverallResultsCalculator orc = new OverallResultsCalculator();

        ResultCalculator rc = new ResultCalculator();
        rc.addEvaluationResults(new EvaluationResultsImpl(1.0, 1.0, 1.0), 1);
        rc.addEvaluationResults(new EvaluationResultsImpl(0.0, 1.0, 0.0), 1);
        orc.addResult(Project.MEDIASTORE, rc);

        ResultCalculator rc2 = new ResultCalculator();
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.9, 0.1, 0.18), 1);
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.6, 0.4, 0.48), 1);
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.3, 0.7, 0.42), 1);
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.9, 0.9, 0.9), 1);
        rc2.addEvaluationResults(new EvaluationResultsImpl(0.6, 0.4, 0.48), 1);
        orc.addResult(Project.TEASTORE, rc2);

        var results = orc.calculateMacroAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(results.getClass().getSimpleName(), EvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.58, results.getPrecision(), 1e-3, "Unexpected Precision"), //
                () -> Assertions.assertEquals(0.75, results.getRecall(), 1e-3, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.496, results.getF1(), 1e-3, "Unexpected F1")//
        );
    }

    @DisplayName("Weighted Average Results With ExtendedEvaluationResults and equal weights")
    @Test
    void weightedAverageResultsWithExtendedEvaluationResults_equalWeights() {
        OverallResultsCalculator orc = new OverallResultsCalculator();

        ResultCalculator rc = new ResultCalculator();
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(1.0, 1.0, 1.0, 1.0, 1.0, 1.0), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.0, 1.0, 0.0, 0.0, 0.0, 0.0), 1);
        orc.addResult(Project.MEDIASTORE, rc);

        ResultCalculator rc2 = new ResultCalculator();
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.9, 0.1, 0.18, 0.6, 0.1, 0.1), 1);
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.6, 0.4, 0.48, 0.7, 0.3, 0.3), 1);
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.3, 0.7, 0.42, 0.5, 0.45, 0.45), 1);
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.9, 0.9, 0.9, 0.4, 0.6, 0.6), 1);
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.6, 0.4, 0.48, 0.3, 0.35, 0.35), 1);
        orc.addResult(Project.TEASTORE, rc2);

        var results = (ExtendedEvaluationResults) orc.calculateWeightedAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(results.getClass().getSimpleName(), ExtendedEvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.614, results.getPrecision(), 1e-3, "Unexpected Precision"), //
                () -> Assertions.assertEquals(0.643, results.getRecall(), 1e-3, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.494, results.getF1(), 1e-3, "Unexpected F1"), //
                () -> Assertions.assertEquals(0.5, results.getAccuracy(), 1e-3, "Unexpected Accuracy"), //
                () -> Assertions.assertEquals(0.4, results.getPhiCoefficient(), 1e-3, "Unexpected Phi Coefficient"), //
                () -> Assertions.assertEquals(0.4, results.getSpecificity(), 1e-3, "Unexpected Specificity") //
        );
    }

    @DisplayName("Weighted Average Results With ExtendedEvaluationResults and unequal weights")
    @Test
    void weightedAverageResultsWithExtendedEvaluationResults_unequalWeights() {
        OverallResultsCalculator orc = new OverallResultsCalculator();

        ResultCalculator rc = new ResultCalculator();
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(1.0, 1.0, 1.0, 1.0, 1.0, 1.0), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.0, 1.0, 0.0, 0.0, 0.0, 0.0), 1);
        orc.addResult(Project.MEDIASTORE, rc);

        ResultCalculator rc2 = new ResultCalculator();
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.9, 0.1, 0.18, 0.6, 0.1, 0.1), 2);
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.6, 0.4, 0.48, 0.7, 0.3, 0.3), 1);
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.3, 0.7, 0.42, 0.5, 0.45, 0.45), 2);
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.9, 0.9, 0.9, 0.4, 0.6, 0.6), 1);
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.6, 0.4, 0.48, 0.3, 0.35, 0.35), 2);
        orc.addResult(Project.TEASTORE, rc2);

        var results = (ExtendedEvaluationResults) orc.calculateWeightedAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(results.getClass().getSimpleName(), ExtendedEvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.61, results.getPrecision(), 1e-3, "Unexpected Precision"), //
                () -> Assertions.assertEquals(0.57, results.getRecall(), 1e-3, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.454, results.getF1(), 1e-3, "Unexpected F1"), //
                () -> Assertions.assertEquals(0.49, results.getAccuracy(), 1e-3, "Unexpected Accuracy"), //
                () -> Assertions.assertEquals(0.37, results.getPhiCoefficient(), 1e-3, "Unexpected Phi Coefficient"), //
                () -> Assertions.assertEquals(0.37, results.getSpecificity(), 1e-3, "Unexpected Specificity") //
        );
    }

    @DisplayName("Macro Average Results With ExtendedEvaluationResults")
    @Test
    void macroAverageResultsWithExtendedEvaluationResults() {
        OverallResultsCalculator orc = new OverallResultsCalculator();

        ResultCalculator rc = new ResultCalculator();
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(1.0, 1.0, 1.0, 1.0, 1.0, 1.0), 1);
        rc.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.0, 1.0, 0.0, 0.0, 0.0, 0.0), 1);
        orc.addResult(Project.MEDIASTORE, rc);

        ResultCalculator rc2 = new ResultCalculator();
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.9, 0.1, 0.18, 0.6, 0.1, 0.1), 2);
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.6, 0.4, 0.48, 0.7, 0.3, 0.3), 1);
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.3, 0.7, 0.42, 0.5, 0.45, 0.45), 2);
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.9, 0.9, 0.9, 0.4, 0.6, 0.6), 1);
        rc2.addEvaluationResults(new ExtendedEvaluationResultsImpl(0.6, 0.4, 0.48, 0.3, 0.35, 0.35), 2);
        orc.addResult(Project.TEASTORE, rc2);

        var results = (ExtendedEvaluationResults) orc.calculateMacroAverageResults();
        Assertions.assertAll( //
                () -> Assertions.assertEquals(results.getClass().getSimpleName(), ExtendedEvaluationResultsImpl.class.getSimpleName()), //
                () -> Assertions.assertEquals(0.569, results.getPrecision(), 1e-3, "Unexpected Precision"), //
                () -> Assertions.assertEquals(0.731, results.getRecall(), 1e-3, "Unexpected Recall"), //
                () -> Assertions.assertEquals(0.471, results.getF1(), 1e-3, "Unexpected F1"), //
                () -> Assertions.assertEquals(0.494, results.getAccuracy(), 1e-3, "Unexpected Accuracy"), //
                () -> Assertions.assertEquals(0.419, results.getPhiCoefficient(), 1e-3, "Unexpected Phi Coefficient"), //
                () -> Assertions.assertEquals(0.419, results.getSpecificity(), 1e-3, "Unexpected Specificity") //
        );
    }
}
