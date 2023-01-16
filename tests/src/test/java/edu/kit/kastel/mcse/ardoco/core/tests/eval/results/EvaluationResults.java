/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval.results;

import java.util.Locale;

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

    /**
     * Returns a string that formats the results in a human-readable manner.
     * 
     * @return a string that formats the results in a human-readable manner
     */
    default String getResultString() {
        return String.format(Locale.ENGLISH, "\tPrecision:%8.2f%n\tRecall:%11.2f%n\tF1:%15.2f", getPrecision(), getRecall(), getF1());
    }

    /**
     * Returns a string that formats the results in a human-readable manner including the given expected results
     * 
     * @param expectedResults the expected results
     * @return a string that formats the results in a human-readable manner
     */
    default String getResultStringWithExpected(ExpectedResults expectedResults) {
        return String.format(Locale.ENGLISH,
                "\tPrecision:%8.2f (min. expected: %.2f)%n\tRecall:%11.2f (min. expected: %.2f)%n\tF1:%15.2f (min. expected: %.2f)", getPrecision(),
                expectedResults.precision(), getRecall(), expectedResults.recall(), getF1(), expectedResults.f1());
    }
}
