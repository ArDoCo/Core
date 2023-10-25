/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.textextraction;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.api.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;

/**
 * An abbreviation with meanings that are phrases. For example, "ArDoCo" is an abbreviation of the phrase "Architecture Documentation Consistency".
 */
public class PhraseAbbreviation extends Disambiguation {
    private final MutableSet<Phrase> phrases;

    public PhraseAbbreviation(@NotNull String abbreviation, @NotNull MutableSet<Phrase> phrases) {
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
    public @NotNull ImmutableSet<Phrase> getPhrases() {
        return phrases.toImmutable();
    }
}
