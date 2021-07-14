package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

public abstract class RecommendationAgent extends Agent {

    protected IText text;
    protected ITextState textState;
    protected IModelState modelState;
    protected IRecommendationState recommendationState;

    /**
     * Prototype Constructor
     */
    protected RecommendationAgent(Class<? extends Configuration> configType) {
        super(configType);
    }

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
        if (data.getText() == null || data.getTextState() == null || data.getModelState() == null || data.getRecommendationState() == null) {
            throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
        }
        return this.create(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), config);
    }

    public abstract RecommendationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            Configuration config);

}
