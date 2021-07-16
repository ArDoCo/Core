package edu.kit.kastel.mcse.ardoco.core.datastructures.extractors;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

/**
 * The Class ConnectionExtractor defines the base class of extractors in the connection generator stage.
 */
public abstract class ConnectionExtractor extends Extractor {

    /** The text state. */
    protected ITextState textState;

    /** The model state. */
    protected IModelState modelState;

    /** The recommendation state. */
    protected IRecommendationState recommendationState;

    /** The connection state. */
    protected IConnectionState connectionState;

    /**
     * Instantiates a new connection extractor.
     *
     * @param textState           the text state
     * @param modelState          the model state
     * @param recommendationState the recommendation state
     * @param connectionState     the connection state
     */
    protected ConnectionExtractor(ITextState textState, IModelState modelState, IRecommendationState recommendationState, IConnectionState connectionState) {
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
        this.connectionState = connectionState;
    }

    @Override
    public ConnectionExtractor create(AgentDatastructure data, Configuration config) {
        Objects.requireNonNull(data.getTextState());
        Objects.requireNonNull(data.getModelState());
        Objects.requireNonNull(data.getRecommendationState());
        Objects.requireNonNull(data.getConnectionState());

        return create(data.getTextState(), data.getModelState(), data.getRecommendationState(), data.getConnectionState(), config);
    }

    /**
     * Creates the extractor.
     *
     * @param textState           the text state
     * @param modelState          the model state
     * @param recommendationState the recommendation state
     * @param connectionState     the connection state
     * @param config              the config
     * @return the connection extractor
     */
    public abstract ConnectionExtractor create(ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, Configuration config);

}
