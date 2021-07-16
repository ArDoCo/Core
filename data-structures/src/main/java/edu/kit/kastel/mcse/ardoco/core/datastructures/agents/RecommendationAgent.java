package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

/**
 * The base class for recommendation agents.
 */
public abstract class RecommendationAgent extends Agent {

    /** The text. */
    protected IText text;

    /** The text state. */
    protected ITextState textState;

    /** The model state. */
    protected IModelState modelState;

    /** The recommendation state. */
    protected IRecommendationState recommendationState;

    /**
     * Prototype Constructor.
     *
     * @param configType the configuration type to be used by the agent
     */
    protected RecommendationAgent(Class<? extends Configuration> configType) {
        super(configType);
    }

    /**
     * Instantiates a new recommendation agent.
     *
     * @param configType          the configuration type to be used by the agent
     * @param text                the text
     * @param textState           the text state
     * @param modelState          the model state
     * @param recommendationState the recommendation state
     */
    protected RecommendationAgent(Class<? extends Configuration> configType, IText text, ITextState textState, IModelState modelState,
            IRecommendationState recommendationState) {
        super(configType);
        this.text = text;
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
    }

    @Override
    protected final RecommendationAgent createInternal(AgentDatastructure data, Configuration config) {
        Objects.requireNonNull(data.getText());
        Objects.requireNonNull(data.getTextState());
        Objects.requireNonNull(data.getModelState());
        Objects.requireNonNull(data.getRecommendationState());

        return this.create(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), config);
    }

    /**
     * Creates the recommendation agent.
     *
     * @param text                the text
     * @param textState           the text state
     * @param modelState          the model state
     * @param recommendationState the recommendation state
     * @param config              the configuration to be used by the agent
     * @return the recommendation agent
     */
    public abstract RecommendationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            Configuration config);

}
