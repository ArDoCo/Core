/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.ComparisonStatGroup;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.ComparisonStatGroup.MeasureStats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

import static java.util.stream.Collectors.toSet;

public class TLStatsFile {

    public static void save(Path targetFile, Map<Project, ComparisonStatGroup> statsMap, Map<Project, AgentDatastructure> dataMap) throws IOException {
        var builder = new StringBuilder();

        // Prepare stat groups
        var totalModelCount = dataMap.values().stream().mapToInt(a -> a.getModelState().getInstances().size()).sum();
        var totalSentenceCount = dataMap.values().stream().mapToInt(a -> a.getText().getSentences().size()).sum();
        var allComparisons = statsMap.values().stream().flatMap(c -> c.getComparisons().stream()).collect(toSet());
        var sumGroup = new ComparisonStatGroup("Σ", totalModelCount, totalSentenceCount, allComparisons);

        var groups = new ArrayList<ComparisonStatGroup>();
        groups.add(sumGroup);
        groups.addAll(statsMap.values().stream().sorted(Comparator.comparing(ComparisonStatGroup::getGroupName)).toList());

        // Global stats
        builder.append("\n# Gold standard stats\n\n");

        builder.append("| BBB \t| #Models \t| #Sentences \t| #Comparisons \t| #Unique Word Pairs \t| #Words \t|\n");
        builder.append("|-------|-----------|--------------|----------------|----------------------|----------|\n");

        for (ComparisonStatGroup group : groups) {
            builder.append("| ").append(group.getGroupName()).append("\t|");
            builder.append(group.getModelCount()).append("\t|");
            builder.append(group.getSentenceCount()).append("\t|");
            builder.append(group.getComparisonCount()).append("\t|");
            builder.append(group.getWordPairCount()).append("\t|");
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
                builder.append(measureStats.measureId()).append("\t|");
                builder.append(measureStats.acceptCount()).append("\t|");
                builder.append(measureStats.denyCount()).append("\t|");
                builder.append(measureStats.uniqueAcceptCount()).append("\t|");
                builder.append('\n');
            }
        }

//        builder.append("\n\n");
//        builder.append("| ? \t| ");
//
//        for (MeasureStats measureStat : sumGroup.getMeasureStats()) {
//            builder.append(measureStat.measureId()).append(" \t|");
//        }
//
//        builder.append("\n");
//
//        for (MeasureStats rowMeasureStats : sumGroup.getMeasureStats()) {
//            builder.append("| ").append(rowMeasureStats.measureId()).append(" \t|");
//
//            for (MeasureStats colMeasureStats : sumGroup.getMeasureStats()) {
//                if (colMeasureStats.measureId().equals(rowMeasureStats.measureId())) {
//                    builder.append(" ? \t|");
//                    continue;
//                }
//
//                rowMeasureStats.acceptedWith().getOrDefault(colMeasureStats.measureId())
//            }
//        }

        builder.append('\n');

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
