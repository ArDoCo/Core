/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.tuple.Pair;

import edu.kit.kastel.mcse.ardoco.core.api.models.LegacyModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.calculator.ResultCalculatorUtil;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.TestLink;

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
    public static void save(Path targetFile, Collection<Pair<GoldStandardProject, EvaluationResults<TestLink>>> results,
            Map<GoldStandardProject, ArDoCoResult> dataMap) throws IOException {
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

    private static void appendProjectResultSummary(Map<GoldStandardProject, ArDoCoResult> dataMap, StringBuilder builder,
            Pair<GoldStandardProject, EvaluationResults<TestLink>> projectResult) {
        var data = dataMap.get(projectResult.getOne());
        var text = data.getText();

        var result = projectResult.getTwo();

        var precision = NUMBER_FORMAT.format(result.precision());
        var recall = NUMBER_FORMAT.format(result.recall());
        var f1Score = NUMBER_FORMAT.format(result.f1());
        var truePosCount = result.truePositives().size();
        var falsePositives = result.falsePositives();
        var falsePosCount = falsePositives.size();
        var falseNegatives = result.falseNegatives();
        var falseNegCount = falseNegatives.size();

        builder.append("# ").append(projectResult.getOne().getProjectName());
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append("Summary:").append(LINE_SEPARATOR);
        builder.append(String.format("- %s Precision / %s Recall / %s F1", precision, recall, f1Score));
        builder.append(LINE_SEPARATOR);
        builder.append(String.format("- %s True Positives / %s False Positives / %s False Negatives", truePosCount, falsePosCount, falseNegCount));
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        if (!falsePositives.isEmpty()) {
            var falsePositivesOutput = createFalseLinksOutput("False Positives", falsePositives.castToList(), data, text);
            builder.append(falsePositivesOutput);
        }

        if (!falseNegatives.isEmpty()) {
            var falseNegativesOutput = createFalseLinksOutput("False Negatives", falseNegatives.castToList(), data, text);
            builder.append(falseNegativesOutput);
        }
    }

    private static <T> void appendOverallResults(List<Pair<GoldStandardProject, EvaluationResults<T>>> projectResults, StringBuilder builder) {
        var results = Lists.mutable.ofAll(projectResults.stream().map(Pair::getTwo).toList());
        var weightedResults = ResultCalculatorUtil.calculateWeightedAverageResults(results.toImmutable());
        var macroResults = ResultCalculatorUtil.calculateAverageResults(results.toImmutable());
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

    static String format(TestLink link, Text text, LegacyModelExtractionState modelState) {
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
