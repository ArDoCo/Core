package edu.kit.kastel.mcse.ardoco.core.api.textextraction;

import java.util.LinkedHashSet;

import org.eclipse.collections.api.set.MutableSet;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * An abbreviation with meanings that are words. For example, "DB" is an abbreviation of the word "Database".
 */
public class WordAbbreviation extends Disambiguation {
    private final MutableSet<Word> words;

    public WordAbbreviation(@NotNull String abbreviation, @NotNull MutableSet<Word> words) {
        super(abbreviation, new LinkedHashSet<>(words.stream().map(Word::getText).toList()));
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
    public MutableSet<Word> getWords() {
        return words;
    }
}
