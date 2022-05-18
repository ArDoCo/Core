/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.EvalProjectResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.EvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.EvalUtils;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.TestLink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class TLDiffFile {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("+##0.00%;-##0.00%");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void save(Path targetFile, EvalResult newResults, EvalResult oldResults,
                            Map<Project, DataStructure> dataMap) throws IOException {

        // Assumption: Both collections contain the same projects

        var builder = new StringBuilder();

        builder.append("Time of evaluation: `").append(DATE_FORMATTER.format(LocalDateTime.now())).append("`\n");

        // Append average differences in precision, recall, f1
        var oldAvgPrecision = oldResults.getPrecision();
        var oldAvgRecall = oldResults.getRecall();
        var oldAvgF1 = oldResults.getF1Score();
        var newAvgPrecision = newResults.getPrecision();
        var newAvgRecall = newResults.getRecall();
        var newAvgF1 = newResults.getF1Score();

        builder.append("Ø ");
        builder.append(NUMBER_FORMAT.format(newAvgPrecision - oldAvgPrecision)).append(" Precision,  ");
        builder.append(NUMBER_FORMAT.format(newAvgRecall - oldAvgRecall)).append(" Recall,  ");
        builder.append(NUMBER_FORMAT.format(newAvgF1 - oldAvgF1)).append(" F1\n\n");

        // Append project specific details
        for (EvalProjectResult oldResult : oldResults.getProjectResults().stream().sorted().toList()) {
            var project = oldResult.getProject();

            var newResult = newResults.getProjectResults().stream()
                    .filter(r -> r.getProject().equals(project))
                    .findAny()
                    .orElse(null);

            if (newResult == null) {
                continue;
            }

            var data = dataMap.get(project);

            builder.append("# ").append(project.name()).append("\n\n");

            builder.append(NUMBER_FORMAT.format(newResult.getPrecision() - oldResult.getPrecision())).append(" Precision,  ");
            builder.append(NUMBER_FORMAT.format(newResult.getRecall() - oldResult.getRecall())).append(" Recall,  ");
            builder.append(NUMBER_FORMAT.format(newResult.getF1Score() - oldResult.getF1Score())).append(" F1\n\n");

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
	        var line = EvalUtils.formatLink(link, data);
	        if (line != null && !line.isBlank()) {
		        builder.append("- ").append(line).append('\n');
	        }
        }

        builder.append('\n');
    }

	private TLDiffFile() { }

}
