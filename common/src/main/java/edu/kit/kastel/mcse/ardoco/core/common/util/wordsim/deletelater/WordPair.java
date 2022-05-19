/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater;

import java.util.Comparator;
import java.util.Objects;

/**
 * A lexicographically sorted pair of words.
 */
public class WordPair implements Comparable<WordPair> {

    public final String firstWord;
    public final String secondWord;

    /**
     * Constructs a new {@link WordPair} instance. Note that word pair instances are sorted. This means that depending
     * on the lexicographical order of the two given words, the second word might end up being the first word of this
     * word pair.
     *
     * @param firstWord  one word of this pair
     * @param secondWord the other word of pair
     */
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
        if (!(o instanceof WordPair wordPair))
            return false;
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
