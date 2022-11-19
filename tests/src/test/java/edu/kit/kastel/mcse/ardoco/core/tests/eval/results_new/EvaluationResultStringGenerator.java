package edu.kit.kastel.mcse.ardoco.core.tests.eval.results_new;

import java.util.Locale;

public class EvaluationResultStringGenerator {

    public static String getResultString(double precision, double recall, double f1) {
        return String.format(Locale.ENGLISH, "\tPrecision:%8.2f%n\tRecall:%11.2f%n\tF1:%15.2f",
                precision, recall, f1);
    }

    public static String getExtendedResultString(double precision, double recall, double f1,
                                                 double accuracy, double specificity,
                                                 double phiCoefficient, double phiOverPhiMax, double phiCoefficientMax) {
        String output = getResultString(precision, recall, f1);
        output += String.format(Locale.ENGLISH, "%n\tAccuracy:%9.2f%n\tSpecificity:%6.2f", accuracy, specificity);
        output += String.format(Locale.ENGLISH, "%n\tPhi Coef.:%8.2f%n\tPhi/PhiMax:%7.2f (Phi Max: %.2f)", phiCoefficient, phiOverPhiMax,
                phiCoefficientMax);
        return output;
    }
}
