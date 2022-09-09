package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

/**
 * This interface represents evaluation results. Implementing classes should be able to return precision, recall, and
 * F1-score of an evaluation.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Precision_and_recall">Wikipedia: Precision and recall</a>
 */
public interface EvaluationResults {
    /**
     * Return the precision.
     * 
     * @return the precision
     */
    double getPrecision();

    /**
     * Return the recall
     * 
     * @return the recall
     */
    double getRecall();

    /**
     * Return the F1-score
     * 
     * @return the F1-score
     */
    double getF1();
}
