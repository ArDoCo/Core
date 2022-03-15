/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.stats.Comparison;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.stats.ComparisonStatGroup;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.stats.MeasureMatrix;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.stats.MeasureStats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TLStatsFile {

    public static void save(Path targetFile, Map<Project, ComparisonStatGroup> statsMap, Map<Project, AgentDatastructure> dataMap) throws IOException {
        if (statsMap.isEmpty()) {
            Files.writeString(targetFile, "disabled", StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return;
        }

        var builder = new StringBuilder();

        // Prepare stat groups
        var totalModelCount = dataMap.values().stream().mapToInt(a -> a.getModelState().getInstances().size()).sum();
        var totalSentenceCount = dataMap.values().stream().mapToInt(a -> a.getText().getSentences().size()).sum();
        var allComparisons = statsMap.values().stream().flatMap(c -> c.getComparisons().stream()).toList();
        var sumGroup = new ComparisonStatGroup("Σ", totalModelCount, totalSentenceCount, allComparisons);

        var groups = new ArrayList<ComparisonStatGroup>();
        groups.add(sumGroup);
        groups.addAll(statsMap.values().stream().sorted(Comparator.comparing(ComparisonStatGroup::getGroupName)).toList());

        // Global stats
        builder.append("\n# Gold standard stats\n\n");

        builder.append("| BBB \t| #Models \t| #Sentences \t| #Comparisons \t| #Words \t|\n");
        builder.append("|-------|-----------|--------------|----------------|----------|\n");

        for (ComparisonStatGroup group : groups) {
            builder.append("| ").append(group.getGroupName()).append("\t|");
            builder.append(group.getModelCount()).append("\t|");
            builder.append(group.getSentenceCount()).append("\t|");
            builder.append(group.getComparisonCount()).append("\t|");
            builder.append(group.getWordCount()).append("\t|");
            builder.append('\n');
        }

        // Measure stats
        builder.append("\n# Measure specific stats\n\n");

        builder.append("| Project | Measure \t| #Accepted \t| #Denied | #Unique Accepts \t|\n");
        builder.append("|---------|-----------|-------------|---------|-------------------|\n");

        for (ComparisonStatGroup group : groups) {
            for (MeasureStats measureStats : group.getMeasureStats()) {
                builder.append("| ");
                builder.append(group.getGroupName()).append("\t|");
                builder.append(measureStats.measureId).append("\t|");
                builder.append(measureStats.accepted.size()).append("\t|");
                builder.append(measureStats.denied.size()).append("\t|");
                builder.append(measureStats.uniquelyAccepted.size()).append("\t|");
                builder.append('\n');
            }
        }

        builder.append("\n");

        // --------------------------------------------------------------------

        builder.append("\n# Measure Matrix\n\n");

        MeasureMatrix matrix = sumGroup.getMatrix();
        List<WordSimMeasure> measures = matrix.getMeasures();

        builder.append("| X \t| ");
        measures.forEach(measure -> builder.append(measure.getClass().getSimpleName()).append(" \t|"));
        builder.append("\n|---|");
        measures.forEach(measure -> builder.append("---|"));
        builder.append("\n");

        for (WordSimMeasure rowMeasure : measures) {
            builder.append("| **").append(rowMeasure.getClass().getSimpleName()).append("** \t| ");

            for (WordSimMeasure colMeasure : measures) {
                int count = matrix.get(rowMeasure, colMeasure);
                builder.append(count).append(" \t| ");
            }

            builder.append("\n");
        }

        builder.append("\n");

        // --------------------------------------------------------------------

        builder.append("# In detail: Uniquely accepted\n\n");

        for (MeasureStats measureStats : sumGroup.getMeasureStats()) {
            if (!measureStats.uniquelyAccepted.isEmpty()) {
                builder.append("### ").append(measureStats.measureId).append("\n\n");

                for (Comparison comparison : measureStats.uniquelyAccepted) {
                    builder.append("- ").append(comparison.ctx().firstString()).append(" & ").append(comparison.ctx().secondString()).append('\n');
                }

                builder.append('\n');
            }

            builder.append('\n');
        }

        builder.append('\n');

        // --------------------------------------------------------------------

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
