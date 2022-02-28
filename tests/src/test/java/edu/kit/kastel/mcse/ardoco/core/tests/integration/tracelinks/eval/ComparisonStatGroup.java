/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonStats.Comparison;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonStats.MeasureResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class ComparisonStatGroup {

    record WordPair(String firstWord, String secondWord) {}

    public record MeasureStats(String measureId, int acceptCount, int denyCount, int uniqueAcceptCount) { }

    private final String groupName;
    private final int modelCount, sentenceCount;
    private final List<Comparison> comparisons = new ArrayList<>();
    private final int comparisonCount;
    private final int wordCount;
    private final int wordPairCount;
    private final List<MeasureStats> measureStats = new ArrayList<>();

    public ComparisonStatGroup(Project project, AgentDatastructure data, Collection<Comparison> comparisons) {
        this(ProjectAlias.getAlias(project), data.getModelState().getInstances().size(), data.getText().getSentences().size(), comparisons);
    }

    public ComparisonStatGroup(String groupName, int modelCount, int sentenceCount, Collection<Comparison> comparisons) {
        this.groupName = groupName;
        this.modelCount = modelCount;
        this.sentenceCount = sentenceCount;
        this.comparisons.addAll(comparisons);

        // Analyze comparisons for gold standard stats
        this.comparisonCount = comparisons.size();
        this.wordCount = Stream.concat(
                comparisons.stream().map(c -> c.ctx().firstString()),
                comparisons.stream().map(c -> c.ctx().secondString())
        ).collect(toSet()).size();
        this.wordPairCount = comparisons.stream().map(c -> {
            String first = c.ctx().firstString();
            String second = c.ctx().secondString();
            if (first.compareTo(second) < 0) { // order strings so that (a,b) and (b,a) cannot both be in this set
                return new WordPair(first, second);
            }
            else {
                return new WordPair(second, first);
            }
        }).collect(toSet()).size();

        // Analyze comparisons for each individual wordsim measure
        var measures = new HashSet<WordSimMeasure>();
        var measureResultToComparisonMap = new HashMap<MeasureResult, Comparison>();
        var measureToMeasureResultMap = new HashMap<WordSimMeasure, List<MeasureResult>>();

        for (Comparison comparison : comparisons) {
            for (MeasureResult measureResult : comparison.results()) {
                measures.add(measureResult.measure());
                measureResultToComparisonMap.put(measureResult, comparison);

                if (!measureToMeasureResultMap.containsKey(measureResult.measure())) {
                    measureToMeasureResultMap.put(measureResult.measure(), new ArrayList<>());
                }

                measureToMeasureResultMap.get(measureResult.measure()).add(measureResult);
            }
        }

        for (WordSimMeasure measure : measures) {
            List<MeasureResult> measureResults = measureToMeasureResultMap.get(measure);
            int acceptCount = 0, denyCount = 0, uniqueAcceptCount = 0;

            for (MeasureResult measureResult : measureResults) {
                Comparison comparison = measureResultToComparisonMap.get(measureResult);

                boolean accepted = measureResult.result();

                if (accepted) {
                    acceptCount++;
                }
                else {
                    denyCount++;
                }

                if (accepted && comparison.results().stream().filter(MeasureResult::result).count() == 1L) {
                    // only one wordsim measure has accepted and current measure has accepted => current measure is the only one that accepted
                    uniqueAcceptCount++;
                }
            }

            this.measureStats.add(new MeasureStats(
                    measure.getClass().getSimpleName(),
                    acceptCount,
                    denyCount,
                    uniqueAcceptCount
            ));
        }
    }

    public String getGroupName() {
        return groupName;
    }

    public int getModelCount() {
        return modelCount;
    }

    public int getSentenceCount() {
        return sentenceCount;
    }

    public List<Comparison> getComparisons() {
        return comparisons;
    }

    public int getComparisonCount() {
        return comparisonCount;
    }

    public int getWordCount() {
        return wordCount;
    }

    public int getWordPairCount() {
        return wordPairCount;
    }

    public List<MeasureStats> getMeasureStats() {
        return measureStats;
    }
}
