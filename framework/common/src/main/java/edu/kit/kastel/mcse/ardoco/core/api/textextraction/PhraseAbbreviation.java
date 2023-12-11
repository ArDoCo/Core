/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.textextraction;

import java.util.LinkedHashSet;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * An abbreviation with meanings that are phrases. For example, "ArDoCo" is an abbreviation of the phrase "Architecture Documentation Consistency".
 */
public class PhraseAbbreviation extends Disambiguation {
    private final LinkedHashSet<Phrase> phrases;

    public PhraseAbbreviation(String abbreviation, LinkedHashSet<Phrase> phrases) {
        super(abbreviation, new TreeSet<>(phrases.stream()
                .map(phrase -> phrase.getContainedWords().stream().map(Word::getText).collect(Collectors.joining(" ")))
                .toList()));
        this.phrases = phrases;
    }

    /**
     * Adds a phrase as meaning to the abbreviation
     *
     * @param phrase the phrase
     */
    public void addPhrase(Phrase phrase) {
        phrases.add(phrase);
    }

    /**
     * {@return the meanings, which are phrases}
     */
    public SortedSet<Phrase> getPhrases() {
        return new TreeSet<>(phrases);
    }
}
