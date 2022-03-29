/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.stats;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

import java.util.List;
import java.util.Optional;

public final class Comparison implements Comparable<Comparison> {

    public final WordPair wordPair;
    public final List<MeasureResult> results;
    public final boolean accepted;

    public Comparison(WordPair wordPair, List<MeasureResult> results, boolean accepted) {
        this.wordPair = wordPair;
        this.results = results;
        this.accepted = accepted;
    }

    public String firstWord() {
        return wordPair.firstWord;
    }

    public String secondWord() {
        return wordPair.secondWord;
    }

    public Optional<MeasureResult> getResultBy(WordSimMeasure measure) {
        return results.stream().filter(mr -> mr.measure().equals(measure)).findAny();
    }

    public Optional<Boolean> hasAccepted(WordSimMeasure measure) {
        return getResultBy(measure).map(MeasureResult::accepted);
    }

    public int getNumberOfAcceptances() {
        return (int) results.stream().filter(MeasureResult::accepted).count();
    }

    @Override public int compareTo(Comparison o) {
        return wordPair.compareTo(o.wordPair);
    }

    @Override public String toString() {
        return "Comparison{" + "wordPair=" + wordPair + ", results=" + results + ", accepted=" + accepted + '}';
    }

    public WordPair wordPair() {
        return wordPair;
    }

    public List<MeasureResult> results() {
        return results;
    }

    public boolean accepted() {
        return accepted;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Comparison))
            return false;

        Comparison that = (Comparison) o;

        return wordPair.equals(that.wordPair);
    }

    @Override public int hashCode() {
        return wordPair.hashCode();
    }
}
