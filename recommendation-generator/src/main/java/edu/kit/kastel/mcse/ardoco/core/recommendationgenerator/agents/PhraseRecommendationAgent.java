/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public final class PhraseRecommendationAgent extends RecommendationAgent {

    @Configurable
    private double confidence = 0.8;

    public PhraseRecommendationAgent() {
    }

    @Override
    public void execute(RecommendationAgentData data) {
        for (var model : data.getModelIds()) {
            var textState = data.getTextState();
            var modelState = data.getModelState(model);
            var recommendationState = data.getRecommendationState(modelState.getMetamodel());
            createRecommendationInstancesFromPhraseNounMappings(textState, recommendationState, modelState);
            findMorePhrasesForRecommendationInstances(textState, recommendationState, modelState);
            findSpecialNamedEntitities(textState, recommendationState);
        }
    }

    /**
     * Look at NounMappings and add RecommendedInstances, if a NounMapping was created because of a phrase (in
     * text-extraction)
     *
     * @param textState
     * @param recommendationState
     */
    private void createRecommendationInstancesFromPhraseNounMappings(ITextState textState, IRecommendationState recommendationState, IModelState modelState) {
        for (var nounMapping : textState.getNounMappings()) {
            if (nounMapping.isPhrase()) {
                var typeMappings = getRelatedTypeMappings(nounMapping, textState);
                addRecommendedInstance(nounMapping, typeMappings, recommendationState, modelState);
            }
        }
    }

    /**
     * Find additional phrases and create RecommendedInstances for them. Additional phrases are when a word in a
     * NounMapping has another word in front or afterwards and that phrase is a TypeMapping
     *
     * @param textState
     * @param recommendationState
     */
    private void findMorePhrasesForRecommendationInstances(ITextState textState, IRecommendationState recommendationState, IModelState modelState) {
        for (var nounMapping : textState.getNounMappings()) {
            for (var word : nounMapping.getWords()) {
                var prevWord = word.getPreWord();
                addRecommendedInstanceIfPhraseWithOtherWord(nounMapping, prevWord, textState, recommendationState, modelState);

                var nextWord = word.getNextWord();
                addRecommendedInstanceIfPhraseWithOtherWord(nounMapping, nextWord, textState, recommendationState, modelState);
            }
        }
    }

    /**
     * Find words that use CamelCase or snake_case.
     *
     * @param textState
     * @param recommendationState
     */
    private void findSpecialNamedEntitities(ITextState textState, IRecommendationState recommendationState) {
        findSpecialNamedEntitiesInNounMappings(textState.getNames(), recommendationState);
    }

    private void findSpecialNamedEntitiesInNounMappings(ImmutableList<INounMapping> nounMappings, IRecommendationState recommendationState) {
        for (var nounMapping : nounMappings) {
            for (var word : nounMapping.getWords()) {
                var wordText = word.getText();
                if (CommonUtilities.isCamelCasedWord(wordText) || CommonUtilities.nameIsSnakeCased(wordText)) {
                    var localNounMappings = Lists.immutable.of(nounMapping);
                    recommendationState.addRecommendedInstance(nounMapping.getReference(), "", confidence, localNounMappings, Lists.immutable.empty());
                }
            }
        }
    }

    private void addRecommendedInstance(INounMapping nounMapping, ImmutableList<INounMapping> typeMappings, IRecommendationState recommendationState,
            IModelState modelState) {
        var nounMappings = Lists.immutable.of(nounMapping);
        var types = getSimilarModelTypes(typeMappings, modelState);
        if (types.isEmpty()) {
            recommendationState.addRecommendedInstance(nounMapping.getReference(), "", confidence, nounMappings, typeMappings);
        } else {
            for (var type : types) {
                recommendationState.addRecommendedInstance(nounMapping.getReference(), type, confidence, nounMappings, typeMappings);
            }
        }
    }

    private ImmutableList<String> getSimilarModelTypes(ImmutableList<INounMapping> typeMappings, IModelState modelState) {
        MutableSet<String> similarModelTypes = Sets.mutable.empty();
        var typeIdentifiers = CommonUtilities.getTypeIdentifiers(modelState);
        for (var typeMapping : typeMappings) {
            var currSimilarTypes = Lists.immutable
                    .fromStream(typeIdentifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, typeMapping.getReference())));
            similarModelTypes.addAll(currSimilarTypes.toList());
            for (var word : typeMapping.getWords()) {
                currSimilarTypes = Lists.immutable
                        .fromStream(typeIdentifiers.stream().filter(typeId -> SimilarityUtils.areWordsSimilar(typeId, word.getLemma())));
                similarModelTypes.addAll(currSimilarTypes.toList());
            }
        }
        return similarModelTypes.toList().toImmutable();
    }

    private ImmutableList<INounMapping> getRelatedTypeMappings(INounMapping nounMapping, ITextState textState) {
        MutableList<INounMapping> typeMappings = Lists.mutable.empty();
        // find TypeMappings that come from the Phrase Words within the Compound Word
        var phrase = getPhraseWordsFromNounMapping(nounMapping);
        for (var word : phrase) {
            typeMappings.addAll(textState.getTypeMappingsByWord(word).toList());
        }
        return typeMappings.toImmutable();
    }

    private void addRecommendedInstanceIfPhraseWithOtherWord(INounMapping nounMapping, IWord word, ITextState textState,
            IRecommendationState recommendationState, IModelState modelState) {
        if (word == null) {
            return;
        }

        if (word.getPosTag().isNoun()) {
            var typeMappings = textState.getMappingsThatCouldBeAType(word);
            if (!typeMappings.isEmpty()) {
                addRecommendedInstance(nounMapping, typeMappings, recommendationState, modelState);
            }
        }
    }

    private static ImmutableList<IWord> getPhraseWordsFromNounMapping(INounMapping nounMapping) {
        ImmutableList<IWord> phrase = Lists.immutable.empty();
        for (var word : nounMapping.getWords()) {
            var currPhrase = CommonUtilities.getCompoundPhrase(word);
            if (currPhrase.size() > phrase.size()) {
                phrase = currPhrase;
            }
        }
        return phrase;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
    }
}
