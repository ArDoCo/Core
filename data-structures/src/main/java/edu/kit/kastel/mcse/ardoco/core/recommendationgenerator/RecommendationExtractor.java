package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.common.Extractor;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

/**
 * The Class RecommendationExtractor defines extractors for the recommendation stage.
 */
public abstract class RecommendationExtractor extends Extractor {

    /** The text state. */
    protected ITextState textState;

    /** The model state. */
    protected IModelState modelState;

    /** The recommendation state. */
    protected IRecommendationState recommendationState;

    /**
     * Instantiates a new recommendation extractor.
     *
     * @param textState           the text state
     * @param modelState          the model state
     * @param recommendationState the recommendation state
     */
    protected RecommendationExtractor(ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
    }

    @Override
    public final RecommendationExtractor create(AgentDatastructure data, Configuration config) {
        Objects.requireNonNull(data.getTextState());
        Objects.requireNonNull(data.getModelState());
        Objects.requireNonNull(data.getRecommendationState());

        return create(data.getTextState(), data.getModelState(), data.getRecommendationState(), config);
    }

    /**
     * Creates the extractor.
     *
     * @param textState           the text state
     * @param modelState          the model state
     * @param recommendationState the recommendation state
     * @param config              the config
     * @return the recommendation extractor
     */
    public abstract RecommendationExtractor create(ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            Configuration config);

}
