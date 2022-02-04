/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.GenericTextConfig;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextAgent;

/**
 * Agent that is responsible for looking at phrases and extracting {@link INounMapping}s from compound nouns etc.
 *
 * @author Jan Keim
 *
 */
@MetaInfServices(TextAgent.class)
public class PhraseAgent extends TextAgent {

    private static final double PHRASE_CONFIDENCE = 0.6;
    private static final double SPECIAL_NAMED_ENTITY_CONFIDENCE = 0.6;

    /**
     * Instantiates a new initial text agent.
     */
    public PhraseAgent() {
        super(GenericTextConfig.class);
    }

    private PhraseAgent(IText text, ITextState textState) {
        super(GenericTextConfig.class, text, textState);
    }

    @Override
    public TextAgent create(IText text, ITextState textState, Configuration config) {
        return new PhraseAgent(text, textState);
    }

    @Override
    public void exec() {
        for (var word : text.getWords()) {
            createNounMappingIfPhrase(word);
            createNounMappingIfSpecialNamedEntity(word);
        }
    }

    private void createNounMappingIfPhrase(IWord word) {
        var phrase = CommonUtilities.getCompoundPhrase(word);

        // if phrase is empty then it is no phrase
        if (phrase.isEmpty()) {
            return;
        }
        // add the full phrase
        addPhraseNounMapping(phrase);

        // filter NounMappings that are types and add the rest of the phrase (if it changed)
        var filteredPhrase = CommonUtilities.filterWordsOfTypeMappings(phrase, textState);
        if (filteredPhrase.size() != phrase.size() && filteredPhrase.size() > 1) {
            addPhraseNounMapping(filteredPhrase);
        }
    }

    private void addPhraseNounMapping(ImmutableList<IWord> phrase) {
        var reference = CommonUtilities.createReferenceForPhrase(phrase);
        var similarReferenceNounMappings = textState.getNounMappingsWithSimilarReference(reference);
        if (similarReferenceNounMappings.isEmpty()) {
            INounMapping phraseNounMapping = NounMapping.createPhraseNounMapping(phrase, PHRASE_CONFIDENCE);
            textState.addNounMapping(phraseNounMapping);
        } else {
            for (var nounMapping : similarReferenceNounMappings) {
                nounMapping.addWords(phrase);
                nounMapping.setAsPhrase(true);
            }
        }
    }

    private void createNounMappingIfSpecialNamedEntity(IWord word) {
        var text = word.getText();
        if (CommonUtilities.isCamelCasedWord(text) || CommonUtilities.nameIsSnakeCased(text)) {
            textState.addName(word, SPECIAL_NAMED_ENTITY_CONFIDENCE);
        }
    }

}
