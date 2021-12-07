/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents;

import java.util.StringJoiner;

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
        var phrase = CommonUtilities.getCompoundPhrases(word);
        phrase = CommonUtilities.filterWordsOfTypeMappings(phrase, textState);
        if (phrase.size() > 1) {
            var reference = createReferenceForPhrase(phrase);
            var similarReferenceNounMappings = textState.getNounMappingsWithSimilarReference(reference);
            if (similarReferenceNounMappings.isEmpty()) {
                INounMapping phraseNounMapping = NounMapping.createPhraseNounMapping(phrase, reference, PHRASE_CONFIDENCE);
                textState.addNounMapping(phraseNounMapping);
            } else {
                for (var nounMapping : similarReferenceNounMappings) {
                    nounMapping.addWords(phrase);
                    nounMapping.setAsPhrase(true);
                }
            }
        }
    }

    private void createNounMappingIfSpecialNamedEntity(IWord word) {
        var text = word.getText();
        if (CommonUtilities.isCamelCasedWord(text) || CommonUtilities.nameIsSnakeCased(text)) {
            textState.addName(word, text, SPECIAL_NAMED_ENTITY_CONFIDENCE);
        }
    }

    private static String createReferenceForPhrase(ImmutableList<IWord> phrase) {
        StringJoiner referenceJoiner = new StringJoiner(" ");
        for (var w : phrase) {
            referenceJoiner.add(w.getText());
        }
        return referenceJoiner.toString();
    }

}
