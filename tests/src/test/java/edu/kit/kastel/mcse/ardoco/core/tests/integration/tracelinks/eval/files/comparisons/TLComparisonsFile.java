/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.comparisons;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.Comparison;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.MeasureResult;

public class TLComparisonsFile {

    public static void save(Path targetFile, Collection<Comparison> comparisons) throws IOException {
        var builder = new StringBuilder();
        builder.append("# ").append(comparisons.size()).append(" Comparisons\n\n");

        var sortedComparisons = new ArrayList<>(comparisons);
        Collections.sort(sortedComparisons);

        for (Comparison comparison : sortedComparisons) {
            appendComparison(builder, comparison);
        }

        builder.append('\n');

        Files.writeString(targetFile, builder.toString(), CREATE, TRUNCATE_EXISTING);
    }

    private static void appendComparison(StringBuilder builder, Comparison comparison) {
        var wordPair = comparison.wordPair();
        var firstWord = wordPair.firstWord;
        var secondWord = wordPair.secondWord;

        builder.append("- ").append(firstWord).append(" & ").append(secondWord).append("");
        builder.append(comparison.accepted() ? " (✓)" : " (❌)");
        builder.append('\n');

        if (!comparison.results().isEmpty()) {
            for (MeasureResult result : comparison.results()) {
                builder.append("  - ").append(result.measure.getClass().getSimpleName()).append(":");
                builder.append(result.accepted ? " ✓" : " ❌");

                if (!Double.isNaN(result.score)) {
                    builder.append(" ").append(result.score);
                }

                builder.append('\n');
            }
        }
    }

    private TLComparisonsFile() {
    }

}
