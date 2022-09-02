/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.map.ImmutableMap;

import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Phrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;

public interface PhraseMapping extends ICopyable<PhraseMapping> {

    ImmutableList<NounMapping> getNounMappings(TextState textState);

    ImmutableList<Phrase> getPhrases();

    PhraseType getPhraseType();

    ImmutableMap<Word, Integer> getPhraseVector();

    void removePhrase(Phrase phrase);

}
