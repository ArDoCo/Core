/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.stats;

import java.util.Comparator;
import java.util.Objects;

public class WordPair implements Comparable<WordPair> {

    public final String firstWord;
    public final String secondWord;

    public WordPair(Comparison comparison) {
        this(comparison.ctx().firstString(), comparison.ctx().secondString());
    }

    public WordPair(String firstWord, String secondWord) {
        Objects.requireNonNull(firstWord);
        Objects.requireNonNull(secondWord);

        if (firstWord.compareTo(secondWord) < 0) {
            this.firstWord = firstWord;
            this.secondWord = secondWord;
        } else {
            this.firstWord = secondWord;
            this.secondWord = firstWord;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WordPair))
            return false;
        WordPair wordPair = (WordPair) o;
        return firstWord.equals(wordPair.firstWord) && secondWord.equals(wordPair.secondWord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstWord, secondWord);
    }

    @Override
    public String toString() {
        return "WordPair(" + firstWord + ", " + secondWord + ")";
    }

    @Override
    public int compareTo(WordPair o) {
        return Comparator.<WordPair, String> comparing(wp -> wp.firstWord).thenComparing(wp -> wp.secondWord).compare(this, o);
    }
}
