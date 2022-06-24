/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.framework.common.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;

public interface IPhraseMapping extends ICopyable<IPhraseMapping> {

    ImmutableList<INounMapping> getNounMappings(ITextState textState);

    ImmutableList<IPhrase> getPhrases();

    void addPhrase(IPhrase phrase);

    void addPhrases(ImmutableList<IPhrase> phrases);

    PhraseType getPhraseType();

    Map<IWord, Integer> getPhraseVector();

    IPhraseMapping merge(IPhraseMapping phraseMapping);

    void removePhrase(IPhrase phrase);

    boolean containsExactNounMapping(ITextState textState, INounMapping nm);
}
