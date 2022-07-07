/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.extractors;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.informalin.framework.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.TextState;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

public class PhraseRecommendationExtractor extends Informant {

    @Configurable
    private double confidence = 0.8;

    public PhraseRecommendationExtractor(DataRepository dataRepository) {
        super("PhraseRecommendationExtractor", dataRepository);
    }

    @Override
    public void run() {
        DataRepository dataRepository = getDataRepository();
        var modelStatesData = DataRepositoryHelper.getModelStatesData(dataRepository);
        var textState = DataRepositoryHelper.getTextState(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);

        for (var model : modelStatesData.modelIds()) {
            var modelState = modelStatesData.getModelState(model);
            var recommendationState = recommendationStates.getRecommendationState(modelState.getMetamodel());

            createRecommendationInstancesFromPhraseNounMappings(textState, recommendationState, modelState);
            findMorePhrasesForRecommendationInstances(textState, recommendationState, modelState);
            findSpecialNamedEntitities(textState, recommendationState);
        }
    }

    /**
     * Look at NounMappings and add RecommendedInstances, if a NounMapping was created because of a phrase (in
     * text-extraction)
     */
    private void createRecommendationInstancesFromPhraseNounMappings(TextState textState, RecommendationState recommendationState,
            ModelExtractionState modelState) {
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
     */
    private void findMorePhrasesForRecommendationInstances(TextState textState, RecommendationState recommendationState, ModelExtractionState modelState) {
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
     */
    private void findSpecialNamedEntitities(TextState textState, RecommendationState recommendationState) {
        findSpecialNamedEntitiesInNounMappings(textState.getNounMappingsOfKind(MappingKind.NAME), recommendationState);
    }

    private void findSpecialNamedEntitiesInNounMappings(ImmutableList<NounMapping> nounMappings, RecommendationState recommendationState) {
        for (var nounMapping : nounMappings) {
            for (var word : nounMapping.getWords()) {
                var wordText = word.getText();
                if (CommonUtilities.isCamelCasedWord(wordText) || CommonUtilities.nameIsSnakeCased(wordText)) {
                    var localNounMappings = Lists.immutable.of(nounMapping);
                    recommendationState.addRecommendedInstance(nounMapping.getReference(), "", this, confidence, localNounMappings, Lists.immutable.empty());
                }
            }
        }
    }

    private void addRecommendedInstance(NounMapping nounMapping, ImmutableList<NounMapping> typeMappings, RecommendationState recommendationState,
            ModelExtractionState modelState) {
        var nounMappings = Lists.immutable.of(nounMapping);
        var types = getSimilarModelTypes(typeMappings, modelState);
        if (types.isEmpty()) {
            recommendationState.addRecommendedInstance(nounMapping.getReference(), "", this, confidence, nounMappings, typeMappings);
        } else {
            for (var type : types) {
                recommendationState.addRecommendedInstance(nounMapping.getReference(), type, this, confidence, nounMappings, typeMappings);
            }
        }
    }

    private ImmutableList<String> getSimilarModelTypes(ImmutableList<NounMapping> typeMappings, ModelExtractionState modelState) {
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

    private ImmutableList<NounMapping> getRelatedTypeMappings(NounMapping nounMapping, TextState textState) {
        MutableList<NounMapping> typeMappings = Lists.mutable.empty();
        // find TypeMappings that come from the Phrase Words within the Compound Word
        var phrase = getPhraseWordsFromNounMapping(nounMapping);
        for (var word : phrase) {
            typeMappings.addAll(textState.getNounMappingsByWordAndKind(word, MappingKind.TYPE).toList());
        }
        return typeMappings.toImmutable();
    }

    private void addRecommendedInstanceIfPhraseWithOtherWord(NounMapping nounMapping, Word word, TextState textState, RecommendationState recommendationState,
            ModelExtractionState modelState) {
        if (word == null) {
            return;
        }

        if (word.getPosTag().isNoun()) {
            var typeMappings = textState.getMappingsThatCouldBeOfKind(word, MappingKind.TYPE);
            if (!typeMappings.isEmpty()) {
                addRecommendedInstance(nounMapping, typeMappings, recommendationState, modelState);
            }
        }
    }

    private static ImmutableList<Word> getPhraseWordsFromNounMapping(NounMapping nounMapping) {
        ImmutableList<Word> phrase = Lists.immutable.empty();
        for (var word : nounMapping.getWords()) {
            var currPhrase = CommonUtilities.getCompoundPhrase(word);
            if (currPhrase.size() > phrase.size()) {
                phrase = currPhrase;
            }
        }
        return phrase;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> map) {
        // empty
    }
}
