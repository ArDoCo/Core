/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests_new.integration.tlrhelper.files;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests_new.integration.tlrhelper.TestLink;
import org.eclipse.collections.api.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.*;

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
     * @param targetFile file to append to
     * @param projectResults    the results to write out
     * @throws IOException if writing to file system fails
     */
    public static void append(Path targetFile, List<Pair<Project, EvaluationResults<TestLink>>> projectResults) throws IOException {
        List<EvaluationResults<TestLink>> results = projectResults.stream().map(Pair::getTwo).toList();
        var builder = new StringBuilder();

        builder.append("- `").append(CommonUtilities.getCurrentTimeAsString()).append("` ");

        // calc average
        double avgPrecision = results.stream().mapToDouble(EvaluationResults::precision).average().orElse(Double.NaN);
        double avgRecall = results.stream().mapToDouble(EvaluationResults::recall).average().orElse(Double.NaN);
        double avgF1 = results.stream().mapToDouble(EvaluationResults::f1).average().orElse(Double.NaN);

        builder.append(String.format("[`Ø`  %s  %s  %s]", NUMBER_FORMAT.format(avgPrecision), NUMBER_FORMAT.format(avgRecall), NUMBER_FORMAT.format(avgF1)));

        var sortedResults = new ArrayList<>(projectResults);
        sortedResults.sort(Comparator.comparing(x -> x.getOne().name()));
        for (Pair<Project, EvaluationResults<TestLink>> projectResult : sortedResults) {
            String alias = switch (projectResult.getOne()) {
            case MEDIASTORE -> "MS";
            case BIGBLUEBUTTON -> "BBB";
            case BIGBLUEBUTTON_HISTORIC -> "BBB-H";
            case TEAMMATES -> "TM";
            case TEAMMATES_HISTORIC -> "TM-H";
            case TEASTORE -> "TS";
            case TEASTORE_HISTORIC -> "TS-H";
            case JABREF -> "JR";
            case JABREF_HISTORIC -> "JR-H";
            };
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
