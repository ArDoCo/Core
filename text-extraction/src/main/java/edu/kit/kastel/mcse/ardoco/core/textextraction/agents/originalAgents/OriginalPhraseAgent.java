/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textextraction.agents.originalAgents;

import java.util.Map;
import java.util.StringJoiner;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

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
@Deprecated
public class OriginalPhraseAgent extends PipelineAgent {
    @Configurable
    private double phraseConfidence = 0.6;
    @Configurable
    private double specialNamedEntityConfidence = 0.6;

    /**
     * Instantiates a new initial text agent.
     */
    public OriginalPhraseAgent(DataRepository dataRepository) {
        super(OriginalPhraseAgent.class.getSimpleName(), dataRepository);
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

            textState.addNounMapping(phrase.toImmutableSet(), MappingKind.NAME, this, phraseConfidence, phrase.toImmutableList(), phrase.collect(Word::getText)
                    .toImmutableSet(), createReferenceForTerm(phrase));
        } else {
            for (var nounMapping : similarReferenceNounMappings) {

                textState.removeNounMapping(nounMapping);

                var newWords = nounMapping.getWords().toSet();
                newWords.addAllIterable(phrase);

                var termMapping = textState.addNounMapping(newWords.toImmutable(), nounMapping.getDistribution().toMap(), nounMapping.getReferenceWords(),
                        nounMapping.getSurfaceForms(), nounMapping.getReference());
                ((NounMappingImpl) termMapping).isDefinedAsTerm().set(true);
            }
        }
    }

    private static String createReferenceForTerm(ImmutableList<Word> phrase) {
        var sortedPhrase = phrase.toSortedListBy(Word::getPosition);
        var referenceJoiner = new StringJoiner(" ");
        for (var w : sortedPhrase) {
            referenceJoiner.add(w.getText());
        }
        return referenceJoiner.toString();
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
