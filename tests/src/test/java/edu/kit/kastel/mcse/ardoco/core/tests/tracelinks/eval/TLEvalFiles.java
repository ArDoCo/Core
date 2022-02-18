/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.tracelinks.eval;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.text.ISentence;

public class TLEvalFiles {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("##0.00%");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static List<TestLink> loadGoldStandardLinks(Project project) throws IOException {
        Path path = Path.of(String.format("src/test/resources/%s/goldstandard.csv", project.name().toLowerCase(Locale.ROOT)));
        List<String> lines = Files.readAllLines(path);

        return lines.stream()
                .skip(1) // skip csv header
                .map(line -> line.split(",")) // modelElementId,sentenceNr
                .map(array -> new TestLink(array[0], Integer.parseInt(array[1])))
                .map(link -> new TestLink(link.modelId(), link.sentenceNr() - 1))
                // ^ goldstandard sentences start with 1 while ISentences are zero indexed
                .toList();
    }

    public static void saveResults(Path targetFile, Collection<TLProjectEvalResult> results) throws IOException {
        var sortedResults = results.stream().sorted().toList();
        var builder = new StringBuilder();

        builder.append("Time of evaluation: `").append(DATE_FORMATTER.format(LocalDateTime.now())).append("`\n\n");

        for (TLProjectEvalResult result : sortedResults) {
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
                    builder.append("- ").append(format(falsePositive, result)).append('\n');
                }

                builder.append('\n');
            }

            if (!result.getFalseNegatives().isEmpty()) {
                builder.append("False Negatives:\n");

                for (TestLink falseNegatives : result.getFalseNegatives()) {
                    builder.append("- ").append(format(falseNegatives, result)).append('\n');
                }

                builder.append('\n');
            }

            builder.append('\n');
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String format(TestLink link, TLProjectEvalResult result) {
        IModelInstance model = result.getModel(link.modelId()).orElse(null);
        ISentence sentence = result.getSentence(link.sentenceNr()).orElse(null);

        String modelStr = model == null ? link.modelId() : "\"" + model.getLongestName() + "\"";
        String sentenceStr = sentence == null ? String.valueOf(link.sentenceNr()) : "\"" + sentence.getText() + "\"";

        return String.format("%s ⇔ %s [%s,%s]", modelStr, sentenceStr, link.modelId(), link.sentenceNr());
    }

}
