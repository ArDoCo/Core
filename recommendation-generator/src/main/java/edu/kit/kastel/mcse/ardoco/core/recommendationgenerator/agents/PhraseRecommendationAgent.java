/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.MutableSet;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonUtilities;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.GenericRecommendationConfig;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

@MetaInfServices(RecommendationAgent.class)
public final class PhraseRecommendationAgent extends RecommendationAgent {

    private double confidence = 0.8;

    public PhraseRecommendationAgent() {
        super(GenericRecommendationConfig.class);
    }

    private PhraseRecommendationAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            GenericRecommendationConfig config) {
        super(GenericRecommendationConfig.class, text, textState, modelState, recommendationState);
        confidence = config.phraseRecommendationConfidence;
    }

    @Override
    public RecommendationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            Configuration config) {
        return new PhraseRecommendationAgent(text, textState, modelState, recommendationState, (GenericRecommendationConfig) config);
    }

    @Override
    public void exec() {
        createRecommendationInstancesFromPhraseNounMappings();
        findMorePhrasesForRecommendationInstances();
        findSpecialNamedEntitities();
    }

    /**
     * Look at NounMappings and add RecommendedInstances, if a NounMapping was created because of a phrase (in
     * text-extraction)
     */
    private void createRecommendationInstancesFromPhraseNounMappings() {
        for (var nounMapping : textState.getNounMappings()) {
            if (nounMapping.isPhrase()) {
                var typeMappings = getRelatedTypeMappings(nounMapping);
                addRecommendedInstance(nounMapping, typeMappings);
            }
        }
    }

    /**
     * Find additional phrases and create RecommendedInstances for them. Additional phrases are when a word in a
     * NounMapping has another word in front or afterwards and that phrase is a TypeMapping
     */
    private void findMorePhrasesForRecommendationInstances() {
        for (var nounMapping : textState.getNounMappings()) {
            for (var word : nounMapping.getWords()) {
                var prevWord = word.getPreWord();
                addRecommendedInstanceIfPhraseWithOtherWord(nounMapping, prevWord);

                var nextWord = word.getNextWord();
                addRecommendedInstanceIfPhraseWithOtherWord(nounMapping, nextWord);
            }
        }
    }

    /**
     * Find words that use CamelCase or snake_case.
     */
    private void findSpecialNamedEntitities() {
        findSpecialNamedEntitiesInNounMappings(textState.getNames());
    }

    private void findSpecialNamedEntitiesInNounMappings(ImmutableList<INounMapping> nounMappings) {
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

    private void addRecommendedInstance(INounMapping nounMapping, ImmutableList<INounMapping> typeMappings) {
        var nounMappings = Lists.immutable.of(nounMapping);
        var types = getSimilarModelTypes(typeMappings);
        if (types.isEmpty()) {
            recommendationState.addRecommendedInstance(nounMapping.getReference(), "", confidence, nounMappings, typeMappings);
        } else {
            for (var type : types) {
                recommendationState.addRecommendedInstance(nounMapping.getReference(), type, confidence, nounMappings, typeMappings);
            }
        }
    }

    private ImmutableList<String> getSimilarModelTypes(ImmutableList<INounMapping> typeMappings) {
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

    private ImmutableList<INounMapping> getRelatedTypeMappings(INounMapping nounMapping) {
        MutableList<INounMapping> typeMappings = Lists.mutable.empty();
        // find TypeMappings that come from the Phrase Words within the Compound Word
        var phrase = getPhraseWordsFromNounMapping(nounMapping);
        for (var word : phrase) {
            typeMappings.addAll(textState.getTypeMappingsByWord(word).toList());
        }
        return typeMappings.toImmutable();
    }

    private void addRecommendedInstanceIfPhraseWithOtherWord(INounMapping nounMapping, IWord word) {
        if (word == null) {
            return;
        }

        if (word.getPosTag().isNoun()) {
            var typeMappings = textState.getMappingsThatCouldBeAType(word);
            if (!typeMappings.isEmpty()) {
                addRecommendedInstance(nounMapping, typeMappings);
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

}
