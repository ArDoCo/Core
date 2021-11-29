package edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval;

public interface EvaluationResult {

    /**
     * @return the f1
     */
    double getF1();

    /**
     * @return the recall
     */
    double getRecall();

    /**
     * @return the precision
     */
    double getPrecision();

}
