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
 * {@link EvaluationResultsImpl}.
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
    public EvaluationResultsImpl calculateWeightedAverageResults() {
        Function<ResultCalculator, EvaluationResults> evaluationResultsFunction = ResultCalculator::getWeightedAverageResults;
        return calculateWeightedAverageResults(evaluationResultsFunction);
    }

    private EvaluationResultsImpl calculateWeightedAverageResults(Function<ResultCalculator, EvaluationResults> evaluationResultsFunction) {
        int weight = 0;
        double precision = .0;
        double recall = .0;
        double f1 = .0;

        for (var entry : projectResults) {
            int localWeight = entry.getTwo().getWeight();
            var resultCalculator = entry.getTwo();
            var results = evaluationResultsFunction.apply(resultCalculator);

            precision += (localWeight * results.getPrecision());
            recall += (localWeight * results.getRecall());
            f1 += (localWeight * results.getF1());
            weight += localWeight;
        }

        precision = precision / weight;
        recall = recall / weight;
        f1 = f1 / weight;

        return new EvaluationResultsImpl(precision, recall, f1);
    }

    /**
     * Calculates the macro average results (precision, recall, F1) as {@link EvaluationResultsImpl}. Each project's result
     * is treated equally and a simple average over all project results is calculated.
     * 
     * @return the macro average results
     */
    public EvaluationResultsImpl calculateMacroAverageResults() {
        int numberOfProjects = projectResults.size();
        double precision = .0;
        double recall = .0;
        double f1 = .0;

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
        }

        precision = precision / numberOfProjects;
        recall = recall / numberOfProjects;
        f1 = f1 / numberOfProjects;

        return new EvaluationResultsImpl(precision, recall, f1);
    }
}
