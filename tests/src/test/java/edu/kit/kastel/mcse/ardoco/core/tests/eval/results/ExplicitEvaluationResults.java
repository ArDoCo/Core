/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationMetrics;

/**
 * This class represents explicit evaluation results. The lists containing the true positives, false negatives, and
 * false positives are explicitly stored and can be retrieved, e.g., for analysing purposes.
 * 
 * @param <T> Type of the lists that store true positives, false negatives, and false positives.
 */
public class ExplicitEvaluationResults<T> extends EvaluationResultsImpl {

    private double precision = -1.337;
    private double recall = -1.337;
    private double f1 = -1.337;

    private ImmutableList<T> truePositives;
    private ImmutableList<T> falseNegatives;
    private ImmutableList<T> falsePositives;

    /**
     * Construct explicit results by providing the lists containing the true positives, false negatives, and false
     * positives.
     * 
     * @param truePositives  the true positives
     * @param falseNegatives the false negatives
     * @param falsePositives the false positives
     */
    public ExplicitEvaluationResults(List<T> truePositives, List<T> falseNegatives, List<T> falsePositives) {
        this.truePositives = Lists.immutable.withAll(truePositives);
        this.falseNegatives = Lists.immutable.withAll(falseNegatives);
        this.falsePositives = Lists.immutable.withAll(falsePositives);
    }

    /**
     * Construct explicit results by providing the lists containing the true positives, false negatives, and false
     * positives.
     *
     * @param truePositives  the true positives
     * @param falseNegatives the false negatives
     * @param falsePositives the false positives
     */
    public ExplicitEvaluationResults(ImmutableList<T> truePositives, ImmutableList<T> falseNegatives, ImmutableList<T> falsePositives) {
        this.truePositives = Lists.immutable.withAll(truePositives);
        this.falseNegatives = Lists.immutable.withAll(falseNegatives);
        this.falsePositives = Lists.immutable.withAll(falsePositives);
    }

    /**
     * Returns the list of false negatives.
     * 
     * @return the list of false negatives
     */
    public ImmutableList<T> getFalseNegatives() {
        return falseNegatives;
    }

    /**
     * Returns the list of false positives.
     * 
     * @return the list of false positives
     */
    public ImmutableList<T> getFalsePositives() {
        return falsePositives;
    }

    /**
     * Returns the list of true positives.
     * 
     * @return the list of true positives
     */
    public ImmutableList<T> getTruePositives() {
        return truePositives;
    }

    @Override
    public double getPrecision() {
        if (precision < 0) {
            precision = EvaluationMetrics.calculatePrecision(truePositives.size(), falsePositives.size());
        }
        return precision;
    }

    @Override
    public double getRecall() {
        if (recall < 0) {
            recall = EvaluationMetrics.calculateRecall(truePositives.size(), falseNegatives.size());
        }
        return recall;
    }

    @Override
    public double getF1() {
        if (f1 < 0) {
            f1 = EvaluationMetrics.calculateF1(truePositives.size(), falsePositives.size(), falseNegatives.size());
        }
        return f1;
    }
}
