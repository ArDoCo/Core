/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;

public interface PhraseMapping {

    ImmutableList<NounMapping> getNounMappings(TextState textState);

    ImmutableList<Phrase> getPhrases();

    PhraseType getPhraseType();

    ImmutableMap<Word, Integer> getPhraseVector();

    void removePhrase(Phrase phrase);

    /**
     * Register a listener that will be notified on certain events.
     *
     * @param listener the listener
     * @see #onDelete(PhraseMapping)
     */
    void registerChangeListener(PhraseMappingChangeListener listener);

    /**
     * Will be invoked during the deletion from a state.
     * Note: This can be invoked multiple times if the replacement is not available during deletion of the phrase mapping
     *
     * @param replacement the replacing new phrase mapping (or null if none exist)
     */
    void onDelete(PhraseMapping replacement);
}
