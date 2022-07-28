/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.TLProjectEvalResult;

public class TLLogFile {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("##0.00%");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void append(Path targetFile, List<TLProjectEvalResult> results) throws IOException {
        var sortedResults = results.stream().sorted().toList();
        var builder = new StringBuilder();

        builder.append("- `").append(DATE_FORMATTER.format(LocalDateTime.now())).append("` ");

        // calc average
        double avgPrecision = results.stream().mapToDouble(TLProjectEvalResult::getPrecision).average().orElse(Double.NaN);
        double avgRecall = results.stream().mapToDouble(TLProjectEvalResult::getRecall).average().orElse(Double.NaN);
        double avgF1 = results.stream().mapToDouble(TLProjectEvalResult::getF1).average().orElse(Double.NaN);

        builder.append(String.format("[`Ø`  %s  %s  %s]", NUMBER_FORMAT.format(avgPrecision), NUMBER_FORMAT.format(avgRecall), NUMBER_FORMAT.format(avgF1)));

        for (TLProjectEvalResult result : sortedResults) {
            String alias = switch (result.getProject()) {
            case BIGBLUEBUTTON -> "BBB";
            case MEDIASTORE -> "MS";
            case TEAMMATES -> "TM";
            case TEASTORE -> "TS";
            };

            String precision = NUMBER_FORMAT.format(result.getPrecision());
            String recall = NUMBER_FORMAT.format(result.getRecall());
            String F1 = NUMBER_FORMAT.format(result.getF1());

            builder.append(String.format(" [`%s`  %s  %s  %s]", alias, precision, recall, F1));
        }

        builder.append('\n');

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

}
