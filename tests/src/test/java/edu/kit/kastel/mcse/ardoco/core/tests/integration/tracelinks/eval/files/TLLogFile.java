/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.EvalProjectResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.EvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.ProjectAlias;

public class TLLogFile {

    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("##0.00%");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void append(Path targetFile, EvalResult evalResult) throws IOException {
        var sortedResults = evalResult.getProjectResults().stream().sorted().toList();
        var builder = new StringBuilder();

        builder.append("- `").append(DATE_FORMATTER.format(LocalDateTime.now())).append("` ");

        // calc average
        double avgPrecision = evalResult.getPrecision();
        double avgRecall = evalResult.getRecall();
        double avgF1 = evalResult.getF1Score();

        builder.append(String.format("[`Ø`  %s  %s  %s]", NUMBER_FORMAT.format(avgPrecision), NUMBER_FORMAT.format(avgRecall), NUMBER_FORMAT.format(avgF1)));

        for (EvalProjectResult result : sortedResults) {
            String alias = ProjectAlias.getAlias(result.getProject());

            String precision = NUMBER_FORMAT.format(result.getPrecision());
            String recall = NUMBER_FORMAT.format(result.getRecall());
            String F1 = NUMBER_FORMAT.format(result.getF1Score());

            builder.append(String.format(" [`%s`  %s  %s  %s]", alias, precision, recall, F1));
        }

        builder.append('\n');

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private TLLogFile() {
    }

}
