/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.TLProjectEvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.TestLink;

public class TLDiffFile {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("+##0.00%;-##0.00%");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void save(Path targetFile, Collection<TLProjectEvalResult> newResults, Collection<TLProjectEvalResult> oldResults,
            Map<Project, DataStructure> dataMap) throws IOException {
        // Assumption: Both collections contain the same projects

        newResults = newResults.stream().sorted().toList();
        oldResults = oldResults.stream().sorted().toList();

        var builder = new StringBuilder();

        builder.append("Time of evaluation: `").append(DATE_FORMATTER.format(LocalDateTime.now())).append("`\n");

        // Append average differences in precision, recall, f1
        var oldAvgPrecision = oldResults.stream().mapToDouble(TLProjectEvalResult::getPrecision).average().orElse(Double.NaN);
        var oldAvgRecall = oldResults.stream().mapToDouble(TLProjectEvalResult::getRecall).average().orElse(Double.NaN);
        var oldAvgF1 = oldResults.stream().mapToDouble(TLProjectEvalResult::getF1).average().orElse(Double.NaN);
        var newAvgPrecision = newResults.stream().mapToDouble(TLProjectEvalResult::getPrecision).average().orElse(Double.NaN);
        var newAvgRecall = newResults.stream().mapToDouble(TLProjectEvalResult::getRecall).average().orElse(Double.NaN);
        var newAvgF1 = newResults.stream().mapToDouble(TLProjectEvalResult::getF1).average().orElse(Double.NaN);

        builder.append("Ø ");
        builder.append(NUMBER_FORMAT.format(newAvgPrecision - oldAvgPrecision)).append(" Precision,  ");
        builder.append(NUMBER_FORMAT.format(newAvgRecall - oldAvgRecall)).append(" Recall,  ");
        builder.append(NUMBER_FORMAT.format(newAvgF1 - oldAvgF1)).append(" F1\n\n");

        // Append project specific details
        for (TLProjectEvalResult oldResult : oldResults) {
            var project = oldResult.getProject();
            var newResultOptional = newResults.stream().filter(r -> r.getProject().equals(project)).findAny();
            if (newResultOptional.isEmpty()) {
                continue;
            }
            var newResult = newResultOptional.get();
            var data = dataMap.get(project);

            builder.append("# ").append(project.name()).append("\n\n");

            builder.append(NUMBER_FORMAT.format(newResult.getPrecision() - oldResult.getPrecision())).append(" Precision,  ");
            builder.append(NUMBER_FORMAT.format(newResult.getRecall() - oldResult.getRecall())).append(" Recall,  ");
            builder.append(NUMBER_FORMAT.format(newResult.getF1() - oldResult.getF1())).append(" F1\n\n");

            var newTruePositives = findNewLinks(oldResult.getTruePositives(), newResult.getTruePositives());
            appendList(builder, "New true positives", newTruePositives, data);

            var newFalsePositives = findNewLinks(oldResult.getFalsePositives(), newResult.getFalsePositives());
            appendList(builder, "New false positives", newFalsePositives, data);

            var newFalseNegatives = findNewLinks(oldResult.getFalseNegatives(), newResult.getFalseNegatives());
            appendList(builder, "New false negatives", newFalseNegatives, data);

            var lostFalsePositives = findMissingLinks(oldResult.getFalsePositives(), newResult.getFalsePositives());
            appendList(builder, "False positives that are now true negatives", lostFalsePositives, data);

            builder.append('\n');
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static List<TestLink> findNewLinks(List<TestLink> oldLinks, List<TestLink> newLinks) {
        return newLinks.stream().filter(link -> !oldLinks.contains(link)).toList();
    }

    private static List<TestLink> findMissingLinks(List<TestLink> oldLinks, List<TestLink> newLinks) {
        return oldLinks.stream().filter(link -> !newLinks.contains(link)).toList();
    }

    private static void appendList(StringBuilder builder, String description, List<TestLink> links, DataStructure data) {
        var text = data.getText();
        if (links.isEmpty()) {
            return;
        }

        builder.append(description).append(":\n");

        for (TestLink link : links) {
            for (var modelId : data.getModelIds()) {
                var datamodel = data.getModelState(modelId);
                var line = TLSummaryFile.format(link, text, datamodel);
                if (line != null && !line.isBlank()) {
                    builder.append("- ").append(line).append('\n');
                }
            }
        }

        builder.append('\n');
    }

}
