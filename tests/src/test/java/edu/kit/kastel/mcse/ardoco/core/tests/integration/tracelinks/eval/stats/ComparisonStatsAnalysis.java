/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.stats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.Comparison;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.MeasureResult;

public class ComparisonStatsAnalysis {

    private final Set<Comparison> comparisons = new HashSet<>();
    private final Set<String> uniqueWords = new HashSet<>();
    private final Set<WordSimMeasure> involvedMeasures = new HashSet<>();
    private final Map<WordSimMeasure, MeasureStats> measureStatsMap = new HashMap<>();
    private final MeasureMatrix measureMatrix;

    public ComparisonStatsAnalysis(Set<Comparison> comparisons) {
        this.comparisons.addAll(comparisons);

        // First pass
        for (Comparison comparison : comparisons) {
            this.uniqueWords.add(comparison.firstWord());
            this.uniqueWords.add(comparison.secondWord());

            for (MeasureResult result : comparison.results) {
                this.involvedMeasures.add(result.measure);

                if (!measureStatsMap.containsKey(result.measure)) {
                    measureStatsMap.put(result.measure, new MeasureStats(result.measure));
                }
            }
        }

        // Second pass
        this.measureMatrix = new MeasureMatrix(this.involvedMeasures);

        for (Comparison comparison : comparisons) {
            for (MeasureResult result : comparison.results) {
                WordSimMeasure measure = result.measure;
                MeasureStats measureStats = measureStatsMap.get(measure);

                if (result.accepted) {
                    measureStats.accepted.add(comparison);

                    if (comparison.getNumberOfAcceptances() == 1) {
                        measureStats.uniquelyAccepted.add(comparison);
                    }

                    for (MeasureResult otherResult : comparison.results) {
                        if (otherResult.accepted) {
                            this.measureMatrix.increment(measure, otherResult.measure);
                        }
                    }
                } else {
                    measureStats.denied.add(comparison);
                }
            }
        }
    }

    public Set<Comparison> getComparisons() {
        return comparisons;
    }

    public Set<String> getUniqueWords() {
        return uniqueWords;
    }

    public Set<WordSimMeasure> getInvolvedMeasures() {
        return involvedMeasures;
    }

    public Map<WordSimMeasure, MeasureStats> getMeasureStatsMap() {
        return measureStatsMap;
    }

    public MeasureMatrix getMeasureMatrix() {
        return measureMatrix;
    }

}
