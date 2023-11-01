/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.textextraction;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.common.collection.UnmodifiableLinkedHashSet;

/**
 * An abbreviation with meanings that are phrases. For example, "ArDoCo" is an abbreviation of the phrase "Architecture Documentation Consistency".
 */
public class PhraseAbbreviation extends Disambiguation {
    private final LinkedHashSet<Phrase> phrases;

    public PhraseAbbreviation(@NotNull String abbreviation, @NotNull LinkedHashSet<Phrase> phrases) {
        super(abbreviation, new LinkedHashSet<>(phrases.stream()
                .map(phrase -> phrase.getContainedWords().stream().map(Word::getText).collect(Collectors.joining(" ")))
                .toList()));
        this.phrases = phrases;
    }

    /**
     * Adds a phrase as meaning to the abbreviation
     *
     * @param phrase the phrase
     */
    public void addPhrase(@NotNull Phrase phrase) {
        phrases.add(phrase);
    }

    /**
     * {@return the meanings, which are phrases}
     */
    public @NotNull UnmodifiableLinkedHashSet<Phrase> getPhrases() {
        return new UnmodifiableLinkedHashSet<>(phrases);
    }
}
