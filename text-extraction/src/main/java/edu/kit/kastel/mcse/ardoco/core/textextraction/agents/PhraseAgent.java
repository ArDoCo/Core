/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.PreprocessingData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Text;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.textextraction.NounMappingImpl;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextStateImpl;

/**
 * Agent that is responsible for looking at phrases and extracting {@link NounMapping}s from compound nouns etc.
 *
 */
public class PhraseAgent extends PipelineAgent {
    @Configurable
    private double phraseConfidence = 0.6;
    @Configurable
    private double specialNamedEntityConfidence = 0.6;

    /**
     * Instantiates a new initial text agent.
     */
    public PhraseAgent(DataRepository dataRepository) {
        super("PhraseAgent", dataRepository);
    }

    @Override
    public void run() {
        var text = getAnnotatedText();
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
        var filteredPhrase = CommonUtilities.filterWordsOfTypeMappings(phrase, textState);
        if (filteredPhrase.size() != phrase.size() && filteredPhrase.size() > 1) {
            addPhraseNounMapping(filteredPhrase, textState);
        }
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
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional config
    }

    private Text getAnnotatedText() {
        return this.getDataRepository().getData(PreprocessingData.ID, PreprocessingData.class).orElseThrow().getText();
    }

}
