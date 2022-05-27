package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IPhrase;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.PhraseType;

public interface IPhraseMapping {

    ImmutableList<INounMapping> getNounMappings();

    ImmutableList<IPhrase> getPhrases();

    void addPhase(IPhrase phrase);

    double getProbability();

    PhraseType getPhraseType();

    Map<IWord, Double> getPhraseVector();
}
