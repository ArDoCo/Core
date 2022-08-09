/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

import java.util.function.Function;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;

public class OverallResultsCalculator {
    // TODO clean up, remove duplicate code etc.

    private MutableList<Pair<Project, ResultCalculator>> projectResults = Lists.mutable.empty();

    public void addResult(Project project, ResultCalculator result) {
        projectResults.add(Tuples.pair(project, result));
    }

    public EvaluationResults getWeightedAveragePRF1() {
        Function<ResultCalculator, EvaluationResults> evaluationResultsFunction = ResultCalculator::getWeightedAveragePRF1;
        return getWeightedAveragePRF1(evaluationResultsFunction);
    }

    public EvaluationResults getMacroWeightedAveragePRF1() {
        Function<ResultCalculator, EvaluationResults> evaluationResultsFunction = ResultCalculator::getMacroAveragePRF1;
        return getWeightedAveragePRF1(evaluationResultsFunction);
    }

    private EvaluationResults getWeightedAveragePRF1(Function<ResultCalculator, EvaluationResults> evaluationResultsFunction) {
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

        return new EvaluationResults(precision, recall, f1);
    }

    public EvaluationResults getMacroAveragePRF1() {
        // TODO
        int numberOfProjects = projectResults.size();
        double precision = .0;
        double recall = .0;
        double f1 = .0;

        for (var entry : projectResults) {
            var results = entry.getTwo().getMacroAveragePRF1();
            precision += results.getPrecision();
            recall += results.getRecall();
            f1 += results.getF1();
        }

        precision = precision / numberOfProjects;
        recall = recall / numberOfProjects;
        f1 = f1 / numberOfProjects;

        return new EvaluationResults(precision, recall, f1);
    }

}
