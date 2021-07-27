package edu.kit.kastel.mcse.ardoco.core.tests;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class EvalUtil {

    private EvalUtil() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    protected static EvaluationResults compare(List<String> traceLinks, List<String> goldStandard) {
        Set<String> distinctTraceLinks = new HashSet<>(traceLinks);
        Set<String> distinctGoldStandard = new HashSet<>(goldStandard);

        // True Positives are the trace links that are contained on both lists
        Set<String> truePositives = distinctTraceLinks.stream().filter(distinctGoldStandard::contains).collect(Collectors.toSet());
        double tp = truePositives.size();

        // False Positives are the trace links that are only contained in the result set
        Set<String> falsePositives = distinctTraceLinks.stream().filter(tl -> !distinctGoldStandard.contains(tl)).collect(Collectors.toSet());
        double fp = falsePositives.size();

        // False Negatives are the trace links that are only contained in the gold standard
        Set<String> falseNegatives = distinctGoldStandard.stream().filter(tl -> !distinctTraceLinks.contains(tl)).collect(Collectors.toSet());
        double fn = falseNegatives.size();

        double precision = tp / (tp + fp);
        double recall = tp / (tp + fn);
        double f1 = 2 * precision * recall / (precision + recall);

        return new EvaluationResults(precision, recall, f1);
    }

}
