/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.collections.api.tuple.Pair;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.TestLink;

/**
 * This helper-class offer functionality to write out a log of the results for TLR.
 */
public class TLLogFile {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("##0.00%");

    private TLLogFile() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Appends the given results to the given file.
     *
     * @param targetFile     file to append to
     * @param projectResults the results to write out
     * @throws IOException if writing to file system fails
     */
    public static void append(Path targetFile, List<Pair<GoldStandardProject, EvaluationResults<TestLink>>> projectResults) throws IOException {
        List<EvaluationResults<TestLink>> results = projectResults.stream().map(Pair::getTwo).toList();
        var builder = new StringBuilder();

        builder.append("- `").append(CommonUtilities.getCurrentTimeAsString()).append("` ");

        // calc average
        double avgPrecision = results.stream().mapToDouble(EvaluationResults::precision).average().orElse(Double.NaN);
        double avgRecall = results.stream().mapToDouble(EvaluationResults::recall).average().orElse(Double.NaN);
        double avgF1 = results.stream().mapToDouble(EvaluationResults::f1).average().orElse(Double.NaN);

        builder.append(String.format("[`Ø`  %s  %s  %s]", NUMBER_FORMAT.format(avgPrecision), NUMBER_FORMAT.format(avgRecall), NUMBER_FORMAT.format(avgF1)));

        var sortedResults = new ArrayList<>(projectResults);
        sortedResults.sort(Comparator.comparing(x -> x.getOne().getProjectName()));
        for (Pair<GoldStandardProject, EvaluationResults<TestLink>> projectResult : sortedResults) {
            String alias = projectResult.getOne().getAlias();
            EvaluationResults<TestLink> result = projectResult.getTwo();
            String precision = NUMBER_FORMAT.format(result.precision());
            String recall = NUMBER_FORMAT.format(result.recall());
            String F1 = NUMBER_FORMAT.format(result.f1());

            builder.append(String.format(" [`%s`  %s  %s  %s]", alias, precision, recall, F1));
        }

        builder.append(LINE_SEPARATOR);

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

}
