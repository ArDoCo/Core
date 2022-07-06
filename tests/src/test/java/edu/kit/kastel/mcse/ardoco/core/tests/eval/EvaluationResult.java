/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.eval;

public interface EvaluationResult {
    double getPrecision();

    double getRecall();

    double getF1();
}
