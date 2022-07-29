/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TLProjectEvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TestLink;

public class TLSummaryFile {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("##0.00%");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void save(Path targetFile, Collection<TLProjectEvalResult> results, Map<Project, ArDoCoResult> dataMap) throws IOException {
        var sortedResults = results.stream().sorted().toList();
        var builder = new StringBuilder();

        var file = new StringBuilder();

        file.append("# Summary\n\n");
        file.append("- Time of evaluation: `").append(DATE_FORMATTER.format(LocalDateTime.now())).append("`\n");

        appendMetrics(file, result.getPrecision(), result.getRecall(), result.getF1Score(), result.getAccuracy(), result.getFalsePositiveCount(),
                result.getFalseNegativeCount(), result.getTruePositiveCount(), result.getTrueNegativeCount());

        for (EvalProjectResult projectResult : result.getProjectResults()) {
            file.append("\n## ").append(projectResult.getProject().name()).append("\n\n");

            appendMetrics(file, projectResult.getPrecision(), projectResult.getRecall(), projectResult.getF1Score(), projectResult.getAccuracy(),
                    projectResult.getFalsePositives().size(), projectResult.getFalseNegatives().size(), projectResult.getTruePositives().size(),
                    projectResult.getTrueNegativeCount());

            if (!projectResult.getFalsePositives().isEmpty()) {
                file.append("\nFalse Positives:\n");
                appendLinks(file, projectResult.getFalsePositives(), dataMap.get(projectResult.getProject()));
            }

            if (!falseNegatives.isEmpty()) {
                var falseNegativesOutput = createFalseLinksOutput("False Negatives", falseNegatives, data, text);
                builder.append(falseNegativesOutput);
            }

            builder.append('\n');
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String createFalseLinksOutput(String type, List<TestLink> falseLinks, ArDoCoResult data, Text text) {
        var builder = new StringBuilder();
        builder.append(type).append(":\n");

        for (TestLink falseLink : falseLinks) {
            builder.append(createFalseLinkOutput(data, text, falseLink));
        }

        builder.append('\n');
        return builder.toString();
    }

    private static String createFalseLinkOutput(ArDoCoResult data, Text text, TestLink falseLink) {
        var builder = new StringBuilder();
        for (var modelId : data.getModelIds()) {
            var datamodel = data.getModelState(modelId);
            var line = format(falseLink, text, datamodel);
            if (line != null && !line.isBlank()) {
                builder.append("- ").append(line).append('\n');
            }
        }

        Files.writeString(targetFile, file.toString(), UTF_8, CREATE, TRUNCATE_EXISTING);
    }

    static String format(TestLink link, Text text, ModelExtractionState modelState) {
        var model = modelState.getInstances().stream().filter(m -> m.getUid().equals(link.modelId())).findAny().orElse(null);
        var sentence = text.getSentences().stream().filter(s -> s.getSentenceNumber() == link.sentenceNr()).findAny().orElse(null);

        if (model == null && sentence == null) {
            return null;
        }
    }

    private static void appendMetrics(StringBuilder file, double precision, double recall, double f1, double accuracy, int falsePositives, int falseNegatives,
            int truePositives, int trueNegatives) {
        file.append(format("- %s Precision, ", NUMBER_FORMAT.format(precision)));
        file.append(format("%s Recall, ", NUMBER_FORMAT.format(recall)));
        file.append(format("%s F1, ", NUMBER_FORMAT.format(f1)));
        file.append(format("%s Accuracy\n", NUMBER_FORMAT.format(accuracy)));

        file.append(format("- %s False Positives, ", falsePositives));
        file.append(format("%s False Negatives, ", falseNegatives));
        file.append(format("%s True Positives, ", truePositives));
        file.append(format("%s True Negatives\n", trueNegatives));
    }

    private TLSummaryFile() {
    }

}
