/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Map;

public interface PhraseMapping extends ICopyable<PhraseMapping> {

    ImmutableList<NounMapping> getNounMappings(TextState textState);

    ImmutableList<Phrase> getPhrases();

    void addPhrase(Phrase phrase);

    void addPhrases(ImmutableList<Phrase> phrases);

    PhraseType getPhraseType();

    Map<Word, Integer> getPhraseVector();

    PhraseMapping merge(PhraseMapping phraseMapping);

    void removePhrase(Phrase phrase);

    boolean containsExactNounMapping(TextState textState, NounMapping nm);
}
