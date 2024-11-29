/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * An abbreviation with meanings that are words. For example, "DB" is an abbreviation of the word "Database".
 */
@Deterministic
public class WordAbbreviation extends Disambiguation {
    private final LinkedHashSet<Word> words;

    public WordAbbreviation(String abbreviation, Set<Word> words) {
        super(abbreviation, new TreeSet<>(words.stream().map(Word::getText).toList()));
        this.words = new LinkedHashSet<>(words);
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

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
