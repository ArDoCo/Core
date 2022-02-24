/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.TLProjectEvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.TestLink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;

public class TLSummaryFile {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("##0.00%");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void save(Path targetFile, Collection<TLProjectEvalResult> results, Map<Project, AgentDatastructure> dataMap) throws IOException {
        var sortedResults = results.stream().sorted().toList();
        var builder = new StringBuilder();

        builder.append("Time of evaluation: `").append(DATE_FORMATTER.format(LocalDateTime.now())).append("`\n\n");

        for (TLProjectEvalResult result : sortedResults) {
            AgentDatastructure data = dataMap.get(result.getProject());

            String precision = NUMBER_FORMAT.format(result.getPrecision());
            String recall = NUMBER_FORMAT.format(result.getRecall());
            String f1Score = NUMBER_FORMAT.format(result.getF1());
            int truePosCount = result.getTruePositives().size();
            int falsePosCount = result.getFalsePositives().size();
            int falseNegCount = result.getFalseNegatives().size();

            builder.append("# ").append(result.getProject().name()).append("\n\n");

            builder.append("Summary:\n");
            builder.append(String.format("- %s Precision / %s Recall / %s F1\n", precision, recall, f1Score));
            builder.append(String.format("- %s True Positives / %s False Positives / %s False Negatives\n", truePosCount, falsePosCount, falseNegCount));
            builder.append('\n');

            if (!result.getFalsePositives().isEmpty()) {
                builder.append("False Positives:\n");

                for (TestLink falsePositive : result.getFalsePositives()) {
                    builder.append("- ").append(format(falsePositive, data)).append('\n');
                }

                builder.append('\n');
            }

            if (!result.getFalseNegatives().isEmpty()) {
                builder.append("False Negatives:\n");

                for (TestLink falseNegatives : result.getFalseNegatives()) {
                    builder.append("- ").append(format(falseNegatives, data)).append('\n');
                }

                builder.append('\n');
            }

            builder.append('\n');
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    static String format(TestLink link, AgentDatastructure data) {
        var model = data.getModelState().getInstances().stream().filter(m -> m.getUid().equals(link.modelId())).findAny().orElse(null);
        var sentence = data.getText().getSentences().stream().filter(s -> s.getSentenceNumber() == link.sentenceNr()).findAny().orElse(null);

        String modelStr = model == null ? link.modelId() : "\"" + model.getLongestName() + "\"";
        String sentenceStr = sentence == null ? String.valueOf(link.sentenceNr()) : "\"" + sentence.getText() + "\"";

        return String.format("%s ⇔ %s [%s,%s]", modelStr, sentenceStr, link.modelId(), link.sentenceNr());
    }

}
