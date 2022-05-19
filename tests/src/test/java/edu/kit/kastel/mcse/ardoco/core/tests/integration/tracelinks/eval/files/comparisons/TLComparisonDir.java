/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.comparisons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.Comparison;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.WordPair;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.stats.ComparisonStatsAnalysis;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.stats.MeasureStats;

public class TLComparisonDir {

    public static void save(Path targetDir, Map<Project, ComparisonStatsAnalysis> statsMap) throws IOException {
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Map<WordPair, Comparison> comparisons = new HashMap<>();
        Map<String, MeasureStats> sumStats = new HashMap<>(); // key is measureId

        for (ComparisonStatsAnalysis group : statsMap.values()) {
            for (Comparison comparison : group.getComparisons()) {
                comparisons.put(comparison.wordPair(), comparison);
            }

            for (MeasureStats measureStat : group.getMeasureStatsMap().values()) {
                if (!sumStats.containsKey(measureStat.measureId)) {
                    sumStats.put(measureStat.measureId, new MeasureStats(measureStat.measure));
                }

                var stats = sumStats.get(measureStat.measureId);
                stats.addFrom(measureStat);
            }
        }

        // Create comparisons file
        Path comparisonsFile = targetDir.resolve("comparisons.txt");
        TLComparisonsFile.save(comparisonsFile, comparisons.values());

        // Create unique accept files for each measure
        for (MeasureStats stats : sumStats.values()) {
            Path uniqueAcceptFile = targetDir.resolve("unique_accepts_" + stats.measureId + ".md");
            TLUniqueAcceptFile.save(uniqueAcceptFile, stats);
        }
    }

    private TLComparisonDir() {
    }

}
