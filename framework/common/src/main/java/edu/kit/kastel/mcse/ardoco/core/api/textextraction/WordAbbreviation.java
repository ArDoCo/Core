/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.textextraction;

import java.util.LinkedHashSet;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * An abbreviation with meanings that are words. For example, "DB" is an abbreviation of the word "Database".
 */
public class WordAbbreviation extends Disambiguation {
    private final LinkedHashSet<Word> words;

    public WordAbbreviation(String abbreviation, LinkedHashSet<Word> words) {
        super(abbreviation, new TreeSet<>(words.stream().map(Word::getText).toList()));
        this.words = words;
    }

    /**
     * Adds a word as meaning to the abbreviation
     *
     * @param word the word
     */
    public void addWord(Word word) {
        words.add(word);
    }

    /**
     * {@return the meanings, which are words}
     */
    public SortedSet<Word> getWords() {
        return new TreeSet<>(words);
    }
}
