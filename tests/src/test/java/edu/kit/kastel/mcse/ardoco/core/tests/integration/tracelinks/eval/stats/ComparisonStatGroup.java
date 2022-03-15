/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.stats;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.stats.Comparison;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.stats.MeasureResult;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.stats.WordPair;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.ProjectAlias;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class ComparisonStatGroup {

    private static List<IModelInstance> getModels(AgentDatastructure data) {
        return data.getModelIds().stream().flatMap(id -> data.getModelState(id).getInstances().stream()).toList();
    }

    private final String groupName;
    private final int modelCount, sentenceCount;
    private final List<Comparison> comparisons;
    private final int comparisonCount;
    private final int wordCount;
    private final List<MeasureStats> measureStats = new ArrayList<>();
    private final MeasureMatrix matrix;

    public ComparisonStatGroup(Project project, AgentDatastructure data, Collection<Comparison> comparisons) {
        this(ProjectAlias.getAlias(project), getModels(data).size(), data.getText().getSentences().size(), comparisons);
    }

    public ComparisonStatGroup(String groupName, int modelCount, int sentenceCount, Collection<Comparison> inputComparisons) {
        this.groupName = groupName;
        this.modelCount = modelCount;
        this.sentenceCount = sentenceCount;

        // 1.) Remove any duplicate comparisons
        Map<WordPair, Comparison> comparisonMap = new HashMap<>();
        for (Comparison comparison : inputComparisons) {
            comparisonMap.put(new WordPair(comparison.ctx().firstString(), comparison.ctx().secondString()), comparison);
        }
        this.comparisons = comparisonMap.values().stream().sorted().toList();

        // 2.) Analyze comparisons for gold standard stats
        this.comparisonCount = comparisons.size();
        this.wordCount = Stream.concat(comparisons.stream().map(c -> c.ctx().firstString()), comparisons.stream().map(c -> c.ctx().secondString()))
                .collect(toSet())
                .size();

        // 3.) Analyze comparisons for each individual word similarity measure
        List<WordSimMeasure> measures = comparisons.stream()
                .flatMap(c -> c.results().stream())
                .map(MeasureResult::measure)
                .collect(toSet())
                .stream()
                .sorted(Comparator.comparing(measure -> measure.getClass().getSimpleName()))
                .toList();

        Map<WordSimMeasure, MeasureStats> map = measures.stream().collect(Collectors.toMap(m -> m, MeasureStats::new));

        this.matrix = new MeasureMatrix(measures);

        for (Comparison comparison : comparisons) {
            for (MeasureResult measureResult : comparison.results()) {
                WordSimMeasure measure = measureResult.measure();
                MeasureStats stats = map.get(measure);

                if (measureResult.accepted()) {
                    stats.accepted.add(comparison);
                } else {
                    stats.denied.add(comparison);
                }

                if (measureResult.accepted() && comparison.getNumberOfAcceptances() == 1) {
                    stats.uniquelyAccepted.add(comparison);
                }

                // Update matrix
                if (measureResult.accepted()) {
                    for (MeasureResult otherResult : comparison.results()) {
                        if (otherResult.accepted()) {
                            this.matrix.increment(measure, otherResult.measure());
                        }
                    }
                }
            }
        }

        this.measureStats.addAll(map.values());
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

    public List<MeasureStats> getMeasureStats() {
        return measureStats;
    }

    public MeasureMatrix getMatrix() {
        return matrix;
    }

}
