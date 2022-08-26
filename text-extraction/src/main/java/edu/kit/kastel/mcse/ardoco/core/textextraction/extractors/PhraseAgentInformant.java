/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.extractors;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.textextraction.NounMappingImpl;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextStateImpl;

public class PhraseAgentInformant extends Informant {
    @Configurable
    private double phraseConfidence = 0.6;
    @Configurable
    private double specialNamedEntityConfidence = 0.6;

    public PhraseAgentInformant(DataRepository dataRepository) {
        super(PhraseAgentInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        var text = DataRepositoryHelper.getAnnotatedText(getDataRepository());
        var textState = getDataRepository().getData(TextState.ID, TextStateImpl.class).orElseThrow();
        for (var word : text.words()) {
            createNounMappingIfPhrase(word, textState);
            createNounMappingIfSpecialNamedEntity(word, textState);
        }
    }

    private void createNounMappingIfPhrase(Word word, TextState textState) {
        var phrase = CommonUtilities.getCompoundPhrase(word);

        // if phrase is empty then it is no phrase
        if (phrase.isEmpty()) {
            return;
        }
        // add the full phrase
        addPhraseNounMapping(phrase, textState);

        // filter NounMappings that are types and add the rest of the phrase (if it changed)
        var filteredPhrase = filterWordsOfTypeMappings(phrase, textState);
        if (filteredPhrase.size() != phrase.size() && filteredPhrase.size() > 1) {
            addPhraseNounMapping(filteredPhrase, textState);
        }
    }

    private ImmutableList<Word> filterWordsOfTypeMappings(ImmutableList<Word> words, TextState textState) {
        MutableList<Word> filteredWords = Lists.mutable.empty();
        for (var word : words) {
            if (!textState.isWordContainedByMappingKind(word, MappingKind.TYPE)) {
                filteredWords.add(word);
            }
        }
        return filteredWords.toImmutable();
    }

    private void addPhraseNounMapping(ImmutableList<Word> phrase, TextState textState) {
        var reference = CommonUtilities.createReferenceForPhrase(phrase);
        var similarReferenceNounMappings = textState.getNounMappingsWithSimilarReference(reference);
        if (similarReferenceNounMappings.isEmpty()) {
            var phraseNounMapping = NounMappingImpl.createPhraseNounMapping(phrase, this, phraseConfidence);
            textState.addNounMapping(phraseNounMapping, this);
        } else {
            for (var nounMapping : similarReferenceNounMappings) {
                nounMapping.addWords(phrase);
                nounMapping.setAsPhrase(true);
            }
        }
    }

    private void createNounMappingIfSpecialNamedEntity(Word word, TextState textState) {
        var text = word.getText();
        if (CommonUtilities.isCamelCasedWord(text) || CommonUtilities.nameIsSnakeCased(text)) {
            textState.addNounMapping(word, MappingKind.NAME, this, specialNamedEntityConfidence);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // none
    }
}
