/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import java.util.Map;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.kohsuke.MetaInfServices;

import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.IExtractor;
import edu.kit.kastel.mcse.ardoco.core.common.Loader;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.GenericRecommendationConfig;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationExtractor;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

/**
 * The Class InitialRecommendationAgent runs all extractors of this stage.
 */
@MetaInfServices(RecommendationAgent.class)
public class InitialRecommendationAgent extends RecommendationAgent {

    private MutableList<IExtractor> extractors = Lists.mutable.empty();

    /**
     * Prototype constructor.
     */
    public InitialRecommendationAgent() {
        super(GenericRecommendationConfig.class);
    }

    private InitialRecommendationAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            GenericRecommendationConfig config) {
        super(GenericRecommendationConfig.class, text, textState, modelState, recommendationState);
        initializeAgents(config.recommendationExtractors, config);
    }

    private void initializeAgents(ImmutableList<String> extractorList, GenericRecommendationConfig config) {
        Map<String, RecommendationExtractor> loadedExtractors = Loader.loadLoadable(RecommendationExtractor.class);

        for (String recommendationExtractor : extractorList) {
            if (!loadedExtractors.containsKey(recommendationExtractor)) {
                throw new IllegalArgumentException("RecommendationExtractor " + recommendationExtractor + " not found");
            }
            extractors.add(loadedExtractors.get(recommendationExtractor).create(textState, modelState, recommendationState, config));
        }
    }

    @Override
    public RecommendationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            Configuration config) {
        return new InitialRecommendationAgent(text, textState, modelState, recommendationState, (GenericRecommendationConfig) config);
    }

    @Override
    public void exec() {
        for (IExtractor extractor : extractors) {
            for (IWord word : text.getWords()) {
                extractor.exec(word);
            }
        }
    }
}
