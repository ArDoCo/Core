package edu.kit.kastel.mcse.ardoco.core.api.textextraction;

import java.util.LinkedHashSet;

import org.eclipse.collections.api.set.MutableSet;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

public class WordAbbreviation extends Disambiguation {
    private final MutableSet<Word> words;

    public WordAbbreviation(@NotNull String abbreviation, @NotNull MutableSet<Word> words) {
        super(abbreviation, new LinkedHashSet<>(words.stream().map(Word::getText).toList()));
        this.words = words;
    }

    public void addWord(Word word) {
        words.add(word);
    }

    public MutableSet<Word> getWords() {
        return words;
    }
}
