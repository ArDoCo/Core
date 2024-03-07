/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.tuple.Pair;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.TestLink;

/**
 * This is a helper class to write out a diff-file for the evaluation results of TLR.
 */
public class TLDiffFile {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("+##0.00%;-##0.00%");
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private TLDiffFile() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Writes out the differences of new and old results.
     *
     * @param targetFile        file to write into
     * @param newProjectResults new results
     * @param oldProjectResults old results
     * @param dataMap           the mapping of Project to ArDoCoResult of the new run
     * @throws IOException if writing fails
     */
    public static void save(Path targetFile, Collection<Pair<GoldStandardProject, EvaluationResults<TestLink>>> newProjectResults,
            Collection<Pair<GoldStandardProject, EvaluationResults<TestLink>>> oldProjectResults, Map<GoldStandardProject, ArDoCoResult> dataMap)
            throws IOException {
        // Assumption: Both collections contain the same projects

        newProjectResults = newProjectResults.stream().sorted(Comparator.comparing(x -> x.getOne().getProjectName())).toList();
        oldProjectResults = oldProjectResults.stream().sorted(Comparator.comparing(x -> x.getOne().getProjectName())).toList();

        var builder = new StringBuilder();

        builder.append("Time of evaluation: `").append(CommonUtilities.getCurrentTimeAsString()).append("`");
        builder.append(LINE_SEPARATOR);

        var newResults = newProjectResults.stream().map(Pair::getTwo).toList();
        var oldResults = newProjectResults.stream().map(Pair::getTwo).toList();

        // Append average differences in precision, recall, f1
        var oldAvgPrecision = oldResults.stream().mapToDouble(EvaluationResults::precision).average().orElse(Double.NaN);
        var oldAvgRecall = oldResults.stream().mapToDouble(EvaluationResults::recall).average().orElse(Double.NaN);
        var oldAvgF1 = oldResults.stream().mapToDouble(EvaluationResults::f1).average().orElse(Double.NaN);
        var newAvgPrecision = newResults.stream().mapToDouble(EvaluationResults::precision).average().orElse(Double.NaN);
        var newAvgRecall = newResults.stream().mapToDouble(EvaluationResults::recall).average().orElse(Double.NaN);
        var newAvgF1 = newResults.stream().mapToDouble(EvaluationResults::f1).average().orElse(Double.NaN);

        builder.append("Ø ");
        builder.append(NUMBER_FORMAT.format(newAvgPrecision - oldAvgPrecision)).append(" Precision,  ");
        builder.append(NUMBER_FORMAT.format(newAvgRecall - oldAvgRecall)).append(" Recall,  ");
        builder.append(NUMBER_FORMAT.format(newAvgF1 - oldAvgF1)).append(" F1");
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        // Append project specific details
        for (Pair<GoldStandardProject, EvaluationResults<TestLink>> oldProjectResult : oldProjectResults) {
            var project = oldProjectResult.getOne();
            var newResultOptional = newProjectResults.stream().filter(r -> r.getOne().equals(project)).findAny();
            if (newResultOptional.isEmpty()) {
                continue;
            }
            var newResult = newResultOptional.get().getTwo();
            var data = dataMap.get(project);

            builder.append("# ").append(project.getProjectName());
            builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

            var oldResult = oldProjectResult.getTwo();
            builder.append(NUMBER_FORMAT.format(newResult.precision() - oldResult.precision())).append(" Precision,  ");
            builder.append(NUMBER_FORMAT.format(newResult.recall() - oldResult.recall())).append(" Recall,  ");
            builder.append(NUMBER_FORMAT.format(newResult.f1() - oldResult.f1())).append(" F1");
            builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

            var newTruePositives = findNewLinks(oldResult.truePositives().castToList(), newResult.truePositives().castToList());
            appendList(builder, "New true positives", newTruePositives, data);

            var newFalsePositives = findNewLinks(oldResult.falsePositives().castToList(), newResult.falsePositives().castToList());
            appendList(builder, "New false positives", newFalsePositives, data);

            var newFalseNegatives = findNewLinks(oldResult.falseNegatives().castToList(), newResult.falseNegatives().castToList());
            appendList(builder, "New false negatives", newFalseNegatives, data);

            var lostFalsePositives = findMissingLinks(oldResult.falsePositives().castToList(), newResult.falsePositives().castToList());
            appendList(builder, "False positives that are now true negatives", lostFalsePositives, data);

            builder.append(LINE_SEPARATOR);
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static List<TestLink> findNewLinks(List<TestLink> oldLinks, List<TestLink> newLinks) {
        return newLinks.stream().filter(link -> !oldLinks.contains(link)).toList();
    }

    private static List<TestLink> findMissingLinks(List<TestLink> oldLinks, List<TestLink> newLinks) {
        return oldLinks.stream().filter(link -> !newLinks.contains(link)).toList();
    }

    private static void appendList(StringBuilder builder, String description, List<TestLink> links, ArDoCoResult arDoCoResult) {
        var text = arDoCoResult.getText();
        if (links.isEmpty()) {
            return;
        }

        builder.append(description).append(":");
        builder.append(LINE_SEPARATOR);

        for (TestLink link : links) {
            for (var modelId : arDoCoResult.getModelIds()) {
                var dataModel = arDoCoResult.getModelState(modelId);
                var line = TLSummaryFile.format(link, text, dataModel);
                if (line != null && !line.isBlank()) {
                    builder.append("- ").append(line).append(LINE_SEPARATOR);
                }
            }
        }

        builder.append(LINE_SEPARATOR);
    }

}
