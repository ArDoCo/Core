/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files;

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

import edu.kit.kastel.mcse.ardoco.core.api.data.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TLProjectEvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TestLink;

public class TLSummaryFile {
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("##0.00%");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private TLSummaryFile() {
        // no instantiation
        throw new IllegalAccessError("No instantiation allowed");
    }

    public static void save(Path targetFile, Collection<TLProjectEvalResult> results, Map<Project, ArDoCoResult> dataMap) throws IOException {
        var sortedResults = results.stream().sorted().toList();
        var builder = new StringBuilder();

        builder.append("Time of evaluation: `").append(DATE_FORMATTER.format(LocalDateTime.now())).append("`\n\n");

        for (TLProjectEvalResult result : sortedResults) {
            var data = dataMap.get(result.getProject());
            var text = data.getText();

            var precision = NUMBER_FORMAT.format(result.getPrecision());
            var recall = NUMBER_FORMAT.format(result.getRecall());
            var f1Score = NUMBER_FORMAT.format(result.getF1());
            var truePosCount = result.getTruePositives().size();
            var falsePositives = result.getFalsePositives();
            var falsePosCount = falsePositives.size();
            var falseNegatives = result.getFalseNegatives();
            var falseNegCount = falseNegatives.size();

            builder.append("# ").append(result.getProject().name()).append("\n\n");

            builder.append("Summary:\n");
            builder.append(String.format("- %s Precision / %s Recall / %s F1\n", precision, recall, f1Score));
            builder.append(String.format("- %s True Positives / %s False Positives / %s False Negatives\n", truePosCount, falsePosCount, falseNegCount));
            builder.append('\n');

            if (!falsePositives.isEmpty()) {
                var falsePositivesOutput = createFalseLinksOutput("False Positives", falsePositives, data, text);
                builder.append(falsePositivesOutput);
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
        return builder.toString();
    }

    static String format(TestLink link, Text text, ModelExtractionState modelState) {
        var model = modelState.getInstances().stream().filter(m -> m.getUid().equals(link.modelId())).findAny().orElse(null);
        var sentence = text.getSentences().stream().filter(s -> s.getSentenceNumber() == link.sentenceNr()).findAny().orElse(null);

        if (model == null && sentence == null) {
            return null;
        }

        var modelStr = model == null ? link.modelId() : "\"" + model.getFullName() + "\"";
        var sentenceStr = sentence == null ? String.valueOf(link.sentenceNr()) : "\"" + sentence.getText() + "\"";

        return String.format("%s ⇔ %s [%s,%s]", modelStr, sentenceStr, link.modelId(), link.sentenceNr());
    }

}
