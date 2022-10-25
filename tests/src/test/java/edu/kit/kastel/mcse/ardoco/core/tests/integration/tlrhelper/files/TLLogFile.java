/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TLProjectEvalResult;

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
     * @param results    the results to write out
     * @throws IOException if writing to file system fails
     */
    public static void append(Path targetFile, List<TLProjectEvalResult> results) throws IOException {
        var sortedResults = results.stream().sorted().toList();
        var builder = new StringBuilder();

        builder.append("- `").append(CommonUtilities.getCurrentTimeAsString()).append("` ");

        // calc average
        double avgPrecision = results.stream().mapToDouble(TLProjectEvalResult::getPrecision).average().orElse(Double.NaN);
        double avgRecall = results.stream().mapToDouble(TLProjectEvalResult::getRecall).average().orElse(Double.NaN);
        double avgF1 = results.stream().mapToDouble(TLProjectEvalResult::getF1).average().orElse(Double.NaN);

        builder.append(String.format("[`Ø`  %s  %s  %s]", NUMBER_FORMAT.format(avgPrecision), NUMBER_FORMAT.format(avgRecall), NUMBER_FORMAT.format(avgF1)));

        for (TLProjectEvalResult result : sortedResults) {
            String alias = switch (result.getProject()) {
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

            String precision = NUMBER_FORMAT.format(result.getPrecision());
            String recall = NUMBER_FORMAT.format(result.getRecall());
            String F1 = NUMBER_FORMAT.format(result.getF1());

            builder.append(String.format(" [`%s`  %s  %s  %s]", alias, precision, recall, F1));
        }

        builder.append(LINE_SEPARATOR);

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

}
