package edu.kit.kastel.mcse.ardoco.core.tests.tracelinks.eval.files;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.tracelinks.eval.TLProjectEvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.tracelinks.eval.TestLink;

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

public class TLDiffFile {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("+##0.00%;-##0.00%");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void save(Path targetFile, Collection<TLProjectEvalResult> newResults, Collection<TLProjectEvalResult> oldResults,
            Map<Project, AgentDatastructure> dataMap) throws IOException {
        // Assumption: Both collections contain the same projects

        newResults = newResults.stream().sorted().toList();
        oldResults = oldResults.stream().sorted().toList();

        var builder = new StringBuilder();

        builder.append("Time of evaluation: `").append(DATE_FORMATTER.format(LocalDateTime.now())).append("`\n");

        // Append average differences in precision, recall, f1
        double oldAvgPrecision = oldResults.stream().mapToDouble(TLProjectEvalResult::getPrecision).average().orElse(Double.NaN);
        double oldAvgRecall = oldResults.stream().mapToDouble(TLProjectEvalResult::getRecall).average().orElse(Double.NaN);
        double oldAvgF1 = oldResults.stream().mapToDouble(TLProjectEvalResult::getF1).average().orElse(Double.NaN);
        double newAvgPrecision = newResults.stream().mapToDouble(TLProjectEvalResult::getPrecision).average().orElse(Double.NaN);
        double newAvgRecall = newResults.stream().mapToDouble(TLProjectEvalResult::getRecall).average().orElse(Double.NaN);
        double newAvgF1 = newResults.stream().mapToDouble(TLProjectEvalResult::getF1).average().orElse(Double.NaN);

        builder.append("Ø ");
        builder.append(NUMBER_FORMAT.format(newAvgPrecision - oldAvgPrecision)).append(" Precision,  ");
        builder.append(NUMBER_FORMAT.format(newAvgRecall - oldAvgRecall)).append(" Recall,  ");
        builder.append(NUMBER_FORMAT.format(newAvgF1 - oldAvgF1)).append(" F1\n\n");

        // Append details
        for (TLProjectEvalResult oldResult : oldResults) {
            Project project = oldResult.getProject();
            TLProjectEvalResult newResult = newResults.stream().filter(r -> r.getProject().equals(project)).findAny().orElseThrow();
            AgentDatastructure data = dataMap.get(project);

            builder.append("# ").append(project.name()).append("\n\n");

            builder.append(NUMBER_FORMAT.format(newResult.getPrecision() - oldResult.getPrecision())).append(" Precision,  ");
            builder.append(NUMBER_FORMAT.format(newResult.getRecall() - oldResult.getRecall())).append(" Recall,  ");
            builder.append(NUMBER_FORMAT.format(newResult.getF1() - oldResult.getF1())).append(" F1\n\n");

            var newTruePositives = findNewLinks(oldResult.getTruePositives(), newResult.getTruePositives());
            if (!newTruePositives.isEmpty()) {
                builder.append("New true positives:\n");

                for (TestLink newTruePositive : newTruePositives) {
                    builder.append("- ").append(TLSummaryFile.format(newTruePositive, data)).append('\n');
                }

                builder.append('\n');
            }

            var newFalsePositives = findNewLinks(oldResult.getFalsePositives(), newResult.getFalsePositives());
            if (!newFalsePositives.isEmpty()) {
                builder.append("New false positives:\n");

                for (TestLink newFalsePositive : newFalsePositives) {
                    builder.append("- ").append(TLSummaryFile.format(newFalsePositive, data)).append('\n');
                }

                builder.append('\n');
            }

            var newFalseNegatives = findNewLinks(oldResult.getFalseNegatives(), newResult.getFalseNegatives());
            if (!newFalseNegatives.isEmpty()) {
                builder.append("New false negatives:\n");

                for (TestLink newFalseNegative : newFalseNegatives) {
                    builder.append("- ").append(TLSummaryFile.format(newFalseNegative, data)).append('\n');
                }

                builder.append('\n');
            }

            builder.append("\n\n");
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static List<TestLink> findNewLinks(List<TestLink> oldLinks, List<TestLink> newLinks) {
        return newLinks.stream().filter(link -> !oldLinks.contains(link)).toList();
    }

}
