/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;

public class TestUtil {

    private TestUtil() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    public static EvaluationResults compare(Collection<String> traceLinks, Collection<String> goldStandard) {
        Set<String> distinctTraceLinks = new HashSet<>(traceLinks);
        Set<String> distinctGoldStandard = new HashSet<>(goldStandard);

        // True Positives are the trace links that are contained on both lists
        Set<String> truePositives = distinctTraceLinks.stream().filter(distinctGoldStandard::contains).collect(Collectors.toSet());
        double tp = truePositives.size();

        // False Positives are the trace links that are only contained in the result set
        Set<String> falsePositives = distinctTraceLinks.stream().filter(tl -> !distinctGoldStandard.contains(tl)).collect(Collectors.toSet());
        List<String> falsePositivesList = new ArrayList<>(falsePositives);
        double fp = falsePositives.size();

        // False Negatives are the trace links that are only contained in the gold standard
        Set<String> falseNegatives = distinctGoldStandard.stream().filter(tl -> !distinctTraceLinks.contains(tl)).collect(Collectors.toSet());
        List<String> falseNegativesList = new ArrayList<>(falseNegatives);
        double fn = falseNegatives.size();

        double precision = tp / (tp + fp);
        double recall = tp / (tp + fn);
        double f1 = 2 * precision * recall / (precision + recall);

        var evaluationResults = new EvaluationResults(precision, recall, f1);
        evaluationResults.setFalseNegatives(Lists.mutable.ofAll(falseNegativesList));
        evaluationResults.setFalsePositives(Lists.mutable.ofAll(falsePositivesList));
        return evaluationResults;
    }

    public static void setConfigOptions(String configFileLocation, String... options) {
        File configFile = new File(configFileLocation);

        try (FileWriter fw = new FileWriter(configFile, false)) {
            for (var option : options) {
                fw.write(option);
                fw.write(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getSimilarityConfigString(double similarity) {
        return "similarityPercentage=" + similarity;
    }

    public static String getMmeiThresholdConfigString(double threshold) {
        return "MissingModelElementInconsistencyAgent_threshold = " + threshold;
    }

}
