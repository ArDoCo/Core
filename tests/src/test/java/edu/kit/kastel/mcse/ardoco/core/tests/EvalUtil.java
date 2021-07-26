package edu.kit.kastel.mcse.ardoco.core.tests;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class EvalUtil {

    private EvalUtil() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    protected static EvaluationResults compare(List<String> traceLinks, List<String> goldStandard) {
        // True Positives are the trace links that are contained on both lists
        Set<String> truePositives = traceLinks.stream().distinct().filter(goldStandard::contains).collect(Collectors.toSet());
        double tp = truePositives.size();

        // False Positives are the trace links that are only contained in the result set
        Set<String> falsePositives = traceLinks.stream().distinct().filter(tl -> !goldStandard.contains(tl)).collect(Collectors.toSet());
        double fp = falsePositives.size();

        // False Negatives are the trace links that are only contained in the gold standard
        Set<String> falseNegatives = goldStandard.stream().distinct().filter(tl -> !traceLinks.contains(tl)).collect(Collectors.toSet());
        double fn = falseNegatives.size();

        double precision = tp / (tp + fp);
        double recall = tp / (tp + fn);
        double f1 = 2 * precision * recall / (precision + recall);

        return new EvaluationResults(precision, recall, f1);
    }

    protected static class EvaluationResults {
        private double precision;
        private double recall;
        private double f1;

        public EvaluationResults(double precision, double recall, double f1) {
            super();
            this.precision = precision;
            this.recall = recall;
            this.f1 = f1;
        }

        public double getPrecision() {
            return precision;
        }

        public double getRecall() {
            return recall;
        }

        public double getF1() {
            return f1;
        }
    }
}
