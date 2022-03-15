/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.stats;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

import java.util.List;
import java.util.Optional;

public record Comparison(ComparisonContext ctx, List<MeasureResult> results, boolean accepted) implements Comparable<Comparison> {

    public Optional<MeasureResult> getResultBy(WordSimMeasure measure) {
        return results.stream().filter(mr -> mr.measure().equals(measure)).findAny();
    }

    public Optional<Boolean> hasAccepted(WordSimMeasure measure) {
        return getResultBy(measure).map(MeasureResult::accepted);
    }

    public WordPair getWordPair() {
        return new WordPair(this);
    }

    public int getNumberOfAcceptances() {
        return (int) results.stream().filter(MeasureResult::accepted).count();
    }

    @Override
    public int compareTo(Comparison o) {
        return getWordPair().compareTo(o.getWordPair());
    }
}
