package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.GenericRecommendationConfig;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
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
        // TODO
    }

    private void createRecommendationInstancesFromPhraseNounMappings() {
        for (var nounMapping : textState.getNounMappings()) {
            // TODO Look if we can extract type information because type is within the compound but was filtered
            if (nounMapping.isPhrase()) {
                var nounMappings = Lists.immutable.of(nounMapping);
                ImmutableList<INounMapping> typeMappings = Lists.immutable.empty();
                String type = "";
                recommendationState.addRecommendedInstance(nounMapping.getReference(), type, CONFIDENCE, nounMappings, typeMappings);
            }
        }
    }

}
