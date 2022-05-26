/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.textextraction.NounMapping;

/**
 * Agent that is responsible for looking at phrases and extracting {@link INounMapping}s from compound nouns etc.
 *
 * @author Jan Keim
 */
public class PhraseAgent extends TextAgent {
    @Configurable
    private double phraseConfidence = 0.6;
    @Configurable
    private double specialNamedEntityConfidence = 0.6;

    /**
     * Instantiates a new initial text agent.
     */
    public PhraseAgent() {
        // empty
    }

    @Override
    public void execute(TextAgentData data) {
        for (var word : data.getText().getWords()) {
            createNounMappingIfPhrase(word, data.getTextState());
            createNounMappingIfSpecialNamedEntity(word, data.getTextState());
        }
    }

    private void createNounMappingIfPhrase(IWord word, ITextState textState) {
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

    private void addPhraseNounMapping(ImmutableList<IWord> phrase, ITextState textState) {
        var reference = CommonUtilities.createReferenceForPhrase(phrase);
        var similarReferenceNounMappings = textState.getNounMappingsWithSimilarReference(reference);
        if (similarReferenceNounMappings.isEmpty()) {
            var phraseNounMapping = NounMapping.createPhraseNounMapping(phrase, this, phraseConfidence);
            textState.addNounMapping(phraseNounMapping, this);
        } else {
            for (var nounMapping : similarReferenceNounMappings) {
                nounMapping.addWords(phrase);
                nounMapping.setAsPhrase(true);
            }
        }
    }

    private void createNounMappingIfSpecialNamedEntity(IWord word, ITextState textState) {
        var text = word.getText();
        if (CommonUtilities.isCamelCasedWord(text) || CommonUtilities.nameIsSnakeCased(text)) {
            textState.addNounMapping(word, MappingKind.NAME, this, specialNamedEntityConfidence);
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // handle additional config
    }
}
