package edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new;


import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationMetrics;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new.types.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new.types.ExplicitEvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new.types.ExtendedEvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new.types.ExtendedExplicitEvaluationResults;

public class EvaluationResultFactory {

    public EvaluationResults createEvaluationResults(ResultMatrix matrix) {
        double precision = EvaluationMetrics.calculatePrecision(matrix.nrTruePos(), matrix.nrFalsePos());
        double recall = EvaluationMetrics.calculateRecall(matrix.nrTruePos(), matrix.nrFalseNeg());
        double f1 = EvaluationMetrics.calculateF1(precision, recall);

        return new EvaluationResults(precision, recall, f1);
    }

    public <T> ExplicitEvaluationResults<T> createExplicitEvaluationResults(ExplicitResultMatrix<T> matrix) {
        ResultMatrix simpleMatrix = matrix.getSimpleMatrix();
        EvaluationResults simpleEvaluationResult = createEvaluationResults(simpleMatrix);

        return new ExplicitEvaluationResults<T>(simpleEvaluationResult.precision(), simpleEvaluationResult.recall(),
                simpleEvaluationResult.f1(),
                matrix.truePositives(), matrix.falseNegatives(), matrix.falsePositives());
    }

    public ExtendedEvaluationResults createExtendedEvaluationResults(ResultMatrix matrix) {
        EvaluationResults simpleEvaluationResult = createEvaluationResults(matrix);

        double accuracy = EvaluationMetrics.calculateAccuracy(matrix.nrTruePos(), matrix.nrFalsePos(),
                matrix.nrFalseNeg(), matrix.nrTrueNeg());
        double phiCoefficient = EvaluationMetrics.calculatePhiCoefficient(matrix.nrTruePos(), matrix.nrFalsePos(),
                matrix.nrFalseNeg(), matrix.nrTrueNeg());
        double specificity = EvaluationMetrics.calculateSpecificity(matrix.nrTrueNeg(), matrix.nrFalsePos());
        double phiCoefficientMax = EvaluationMetrics.calculatePhiCoefficientMax(matrix.nrTruePos(), matrix.nrFalsePos(),
                matrix.nrFalseNeg(), matrix.nrTrueNeg());
        double phiOverPhiMax = EvaluationMetrics.calculatePhiOverPhiMax(matrix.nrTruePos(), matrix.nrFalsePos(),
                matrix.nrFalseNeg(), matrix.nrTrueNeg());

        return new ExtendedEvaluationResults(simpleEvaluationResult.precision(), simpleEvaluationResult.recall(), simpleEvaluationResult.f1(),
                accuracy, phiCoefficient, specificity, phiCoefficientMax, phiOverPhiMax);
    }

    public <T> ExtendedExplicitEvaluationResults<T> createExtendedExplicitEvaluationResults(ExplicitResultMatrix<T> matrix) {
        ExtendedEvaluationResults extendedResult = createExtendedEvaluationResults(matrix.getSimpleMatrix());

        return new ExtendedExplicitEvaluationResults<T>(extendedResult.precision(), extendedResult.recall(), extendedResult.f1(),
                matrix.truePositives(), matrix.falseNegatives(), matrix.falsePositives(),
                extendedResult.accuracy(), extendedResult.phiCoefficient(), extendedResult.specificity(), extendedResult.phiCoefficientMax(),
                extendedResult.phiOverPhiMax(), matrix.trueNegatives().size());
    }
}
