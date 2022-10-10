/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

import java.util.function.Function;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;

/**
 * This class implements functionality to calculate overall results (averaged) when evaluating a number of projects.
 * There are different kinds of averages that can be calculated and that each return an instance of
 * {@link EvaluationResults}, in some circumstances even {@link ExtendedEvaluationResults}.
 */
public class OverallResultsCalculator {

    private MutableList<Pair<Project, ResultCalculator>> projectResults = Lists.mutable.empty();

    /**
     * Adds a result of a project to this calculator.
     * 
     * @param project Project the results belong to
     * @param result  the ResultCalculator for the results
     */
    public void addResult(Project project, ResultCalculator result) {
        projectResults.add(Tuples.pair(project, result));
    }

    /**
     * Calculates the weighted average results (precision, recall, F1) as {@link EvaluationResultsImpl}. Each project's
     * result is weighted with the number of expected instances (= true positives + false negatives). This method uses
     * weighted results for each project if there are multiple runs per project, e.g., for holdback inconsistency
     * detection evaluation.
     * 
     * @return the weighted average results
     */
    public EvaluationResults calculateWeightedAverageResults() {
        Function<ResultCalculator, EvaluationResults> evaluationResultsFunction = ResultCalculator::getWeightedAverageResults;
        return calculateWeightedAverageResults(evaluationResultsFunction);
    }

    private EvaluationResults calculateWeightedAverageResults(Function<ResultCalculator, EvaluationResults> evaluationResultsFunction) {
        double weight = 0.0;
        double precision = .0;
        double recall = .0;
        double f1 = .0;
        double accuracy = .0;
        double phi = .0;
        double specificity = .0;

        for (var entry : projectResults) {
            double localWeight = entry.getTwo().getWeight();
            var resultCalculator = entry.getTwo();
            var results = evaluationResultsFunction.apply(resultCalculator);

            precision += (localWeight * results.getPrecision());
            recall += (localWeight * results.getRecall());
            f1 += (localWeight * results.getF1());
            weight += localWeight;

            if (results instanceof ExtendedEvaluationResults extendedResults) {
                accuracy += (localWeight * extendedResults.getAccuracy());
                specificity += (localWeight * extendedResults.getSpecificity());
                phi += (localWeight * extendedResults.getPhiCoefficient());
            }
        }

        precision = precision / weight;
        recall = recall / weight;
        f1 = f1 / weight;

        if (phi != 0.0 && accuracy > 0.0) {
            phi = phi / weight;
            accuracy = accuracy / weight;
            specificity = specificity / weight;
            return new ExtendedEvaluationResultsImpl(precision, recall, f1, accuracy, phi, specificity);
        }
        return new EvaluationResultsImpl(precision, recall, f1);
    }

    /**
     * Calculates the macro average results (precision, recall, F1) as {@link EvaluationResultsImpl}. Each project's result
     * is treated equally and a simple average over all project results is calculated.
     * 
     * @return the macro average results
     */
    public EvaluationResults calculateMacroAverageResults() {
        int numberOfProjects = projectResults.size();
        double precision = .0;
        double recall = .0;
        double f1 = .0;
        double accuracy = .0;
        double phi = .0;
        double specificity = .0;

        for (var entry : projectResults) {
            var results = entry.getTwo().getWeightedAverageResults();
            int weight = entry.getTwo().getWeight();
            if (weight <= 0) {
                numberOfProjects--;
                continue;
            }
            precision += results.getPrecision();
            recall += results.getRecall();
            f1 += results.getF1();

            if (results instanceof ExtendedEvaluationResults extendedResult) {
                phi += extendedResult.getPhiCoefficient();
                accuracy += extendedResult.getAccuracy();
                specificity += extendedResult.getSpecificity();
            }
        }

        precision = precision / numberOfProjects;
        recall = recall / numberOfProjects;
        f1 = f1 / numberOfProjects;

        if (phi != 0.0 && accuracy > 0.0) {
            phi /= numberOfProjects;
            accuracy /= numberOfProjects;
            specificity /= numberOfProjects;
            return new ExtendedEvaluationResultsImpl(precision, recall, f1, accuracy, phi, specificity);
        }
        return new EvaluationResultsImpl(precision, recall, f1);
    }
}
