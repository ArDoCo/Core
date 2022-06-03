package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.Confidence;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;

public interface IPhraseMapping {

    void addNounMapping(INounMapping nounMapping, IPhrase phrase);

    ImmutableList<INounMapping> getNounMappings();

    ImmutableList<IPhrase> getPhrases();

    void addPhrase(IPhrase phrase);

    void addPhrases(ImmutableList<IPhrase> phrases);

    double getProbability();

    PhraseType getPhraseType();

    Map<IWord, Integer> getPhraseVector();

    IPhraseMapping merge(IPhraseMapping phraseMapping);

    Confidence getConfidence();

    void removeNounMapping(INounMapping nounMapping);
}
