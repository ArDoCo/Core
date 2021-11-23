package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import java.util.Set;

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
public class PhraseRecommendationAgent extends RecommendationAgent {

    private static final double CONFIDENCE = 0.8;

    /**
     * Prototype constructor.
     */
    public PhraseRecommendationAgent() {
        super(GenericRecommendationConfig.class);
    }

    private PhraseRecommendationAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            GenericRecommendationConfig config) {
        super(GenericRecommendationConfig.class, text, textState, modelState, recommendationState);
    }

    @Override
    public RecommendationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            Configuration config) {
        return new PhraseRecommendationAgent(text, textState, modelState, recommendationState, (GenericRecommendationConfig) config);
    }

    @Override
    public void exec() {
        createRecommendationInstancesFromPhraseNounMappings();
    }

    private void createRecommendationInstancesFromPhraseNounMappings() {
        for (var nounMapping : textState.getNounMappings()) {
            if (nounMapping.isPhrase()) {
                var nounMappings = Lists.immutable.of(nounMapping);
                var typeMappings = getRelatedTypeMappings(nounMapping);
                var types = getSimilarModelTypes(typeMappings);
                if (types.isEmpty()) {
                    recommendationState.addRecommendedInstance(nounMapping.getReference(), "", CONFIDENCE, nounMappings, typeMappings);
                } else {
                    for (var type : types) {
                        recommendationState.addRecommendedInstance(nounMapping.getReference(), type, CONFIDENCE, nounMappings, typeMappings);
                    }
                }
            }
        }
    }

    private ImmutableList<String> getSimilarModelTypes(ImmutableList<INounMapping> typeMappings) {
        MutableSet<String> similarModelTypes = Sets.mutable.empty();
        Set<String> typeIdentifiers = CommonUtilities.getTypeIdentifiers(modelState);
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
        // TODO find further TypeMappings around, if possible!
        return typeMappings.toImmutable();
    }

    private static ImmutableList<IWord> getPhraseWordsFromNounMapping(INounMapping nounMapping) {
        ImmutableList<IWord> phrase = Lists.immutable.empty();
        for (var word : nounMapping.getWords()) {
            var currPhrase = CommonUtilities.getCompoundPhrases(word);
            if (currPhrase.size() > phrase.size()) {
                phrase = currPhrase;
            }
        }
        return phrase;
    }

}
