/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TLProjectEvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TestLink;

/**
 * This helper class offers functionality to write out a summary of the TLR evaluation runs for all projects.
 */
public class TLSummaryFile {
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("##0.00%");
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private TLSummaryFile() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Writes a summary of the given results, data etc. to the given file.
     *
     * @param targetFile file to write to
     * @param results    results of the runs
     * @param dataMap    the outcomes (data) of the runs
     * @throws IOException if writing to file system fails
     */
    public static void save(Path targetFile, Collection<TLProjectEvalResult> results, Map<Project, ArDoCoResult> dataMap) throws IOException {
        var sortedResults = results.stream().sorted().toList();
        var builder = new StringBuilder();

        builder.append("Time of evaluation: `").append(CommonUtilities.getCurrentTimeAsString()).append("`");
        builder.append(LINE_SEPARATOR);

        appendOverallResults(sortedResults, builder);

        for (var result : sortedResults) {
            appendProjectResultSummary(dataMap, builder, result);
            builder.append(LINE_SEPARATOR);
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static void appendProjectResultSummary(Map<Project, ArDoCoResult> dataMap, StringBuilder builder, TLProjectEvalResult result) {
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

        builder.append("# ").append(result.getProject().name());
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append("Summary:").append(LINE_SEPARATOR);
        builder.append(String.format("- %s Precision / %s Recall / %s F1", precision, recall, f1Score));
        builder.append(LINE_SEPARATOR);
        builder.append(String.format("- %s True Positives / %s False Positives / %s False Negatives", truePosCount, falsePosCount, falseNegCount));
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        if (!falsePositives.isEmpty()) {
            var falsePositivesOutput = createFalseLinksOutput("False Positives", falsePositives, data, text);
            builder.append(falsePositivesOutput);
        }

        if (!falseNegatives.isEmpty()) {
            var falseNegativesOutput = createFalseLinksOutput("False Negatives", falseNegatives, data, text);
            builder.append(falseNegativesOutput);
        }
    }

    private static void appendOverallResults(List<TLProjectEvalResult> results, StringBuilder builder) {
        var overallResultsCalculator = TestUtil.getOverallResultsCalculator(results);
        var weightedResults = overallResultsCalculator.calculateWeightedAverageResults();
        var macroResults = overallResultsCalculator.calculateMacroAverageResults();
        var resultString = TestUtil.createResultLogString("Overall Weighted", weightedResults);
        builder.append(resultString).append(LINE_SEPARATOR);
        resultString = TestUtil.createResultLogString("Overall Macro", macroResults);
        builder.append(resultString).append(LINE_SEPARATOR).append(LINE_SEPARATOR);
    }

    private static String createFalseLinksOutput(String type, List<TestLink> falseLinks, ArDoCoResult data, Text text) {
        var builder = new StringBuilder();
        builder.append(type).append(":").append(LINE_SEPARATOR);

        for (TestLink falseLink : falseLinks) {
            builder.append(createFalseLinkOutput(data, text, falseLink));
        }

        builder.append(LINE_SEPARATOR);
        return builder.toString();
    }

    private static String createFalseLinkOutput(ArDoCoResult data, Text text, TestLink falseLink) {
        var builder = new StringBuilder();
        for (var modelId : data.getModelIds()) {
            var dataModel = data.getModelState(modelId);
            var line = format(falseLink, text, dataModel);
            if (line != null && !line.isBlank()) {
                builder.append("- ").append(line).append(LINE_SEPARATOR);
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
