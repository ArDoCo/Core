/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.comparisons;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.Comparison;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.MeasureResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.stats.MeasureStats;

public class TLUniqueAcceptFile {

    public static void save(Path targetFile, MeasureStats measureStats) throws IOException {
        var builder = new StringBuilder();

        var comparisons = new ArrayList<>(measureStats.uniquelyAccepted);
        Collections.sort(comparisons);

        builder.append("# ").append(comparisons.size()).append(" unique acceptances by `").append(measureStats.measureId).append("`\n\n");

        for (Comparison comparison : measureStats.uniquelyAccepted) {
            appendComparison(builder, comparison);
        }

        builder.append('\n');

        Files.writeString(targetFile, builder.toString(), CREATE, TRUNCATE_EXISTING);
    }

    private static void appendComparison(StringBuilder builder, Comparison comparison) {
        var wordPair = comparison.wordPair();
        var firstWord = wordPair.firstWord;
        var secondWord = wordPair.secondWord;

        builder.append("- `").append(firstWord).append("` & `").append(secondWord).append("`");
        builder.append(comparison.accepted() ? " (✓)" : " (❌)");
        builder.append('\n');

        if (!comparison.results().isEmpty()) {
            for (MeasureResult result : comparison.results()) {
                builder.append("  - ").append(result.measure.getClass().getSimpleName()).append(":");
                builder.append(result.accepted ? " ✓" : " ❌");

                if (!Double.isNaN(result.score)) {
                    builder.append(" `").append(result.score).append("`");
                }

                builder.append('\n');
            }
        }
    }

    private TLUniqueAcceptFile() {
    }

}
