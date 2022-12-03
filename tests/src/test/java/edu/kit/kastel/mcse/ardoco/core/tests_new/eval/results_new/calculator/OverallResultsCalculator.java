/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results_new.calculator;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results_new.EvaluationResults;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

/**
 * This class implements functionality to calculate overall results (averaged) when evaluating a number of projects.
 * There are different kinds of averages that can be calculated and that each return an instance of
 * {@link edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults}
 *
 * @param <T>
 */
public class OverallResultsCalculator <T>{

    private final MutableList<Pair<Project, ResultCalculator<T>>> projectResults = Lists.mutable.empty();

    /**
     * Adds a result of a project to this calculator.
     * 
     * @param project Project the results belong to
     * @param result  the ResultCalculator for the results
     */
    public void addResult(Project project, ResultCalculator<T> result) {
        projectResults.add(Tuples.pair(project, result));
    }

    /**
     * Calculates the weighted average results (precision, recall, F1). Each project's
     * result is weighted with the number of expected instances (= true positives + false negatives). This method uses
     * weighted results for each project if there are multiple runs per project, e.g., for holdback inconsistency
     * detection evaluation.
     * 
     * @return the weighted average results
     */
    public EvaluationResults<T> calculateWeightedAverageResults() {
        MutableList<Pair<EvaluationResults<T>, Integer>> resultsWithWeight = Lists.mutable.empty();
        resultsWithWeight.addAll(projectResults.stream()
                .map(x -> Tuples.pair(x.getTwo().getWeightedAverageResults(), x.getTwo().getWeight()))
                .toList());
        return ResultCalculatorUtil.calculateWeightedAverageResults(resultsWithWeight);
    }

    /**
     * Calculates the macro average results (precision, recall, F1). Each project's result
     * is treated equally and a simple average over all project results is calculated.
     * 
     * @return the macro average results
     */
    public EvaluationResults<T> calculateMacroAverageResults() {
        int numberOfProjects = projectResults.size();
        MutableList<Pair<EvaluationResults<T>, Integer>> resultsWithWeight = Lists.mutable.empty();
        resultsWithWeight.addAll(projectResults.stream()
                .map(x -> Tuples.pair(x.getTwo().getWeightedAverageResults(), x.getTwo().getWeight()))
                .toList());
        return ResultCalculatorUtil.calculateAverageResults(numberOfProjects, resultsWithWeight);
    }
}
