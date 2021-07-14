package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

public abstract class DependencyAgent extends Agent {

    protected IText text;
    protected ITextState textState;
    protected IModelState modelState;
    protected IRecommendationState recommendationState;

    protected DependencyAgent(Class<? extends Configuration> configType) {
        super(configType);
    }

    protected DependencyAgent(DependencyType dependencyType, Class<? extends Configuration> configType, IText text,
                              ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
        super(dependencyType, configType);
        this.text = text;
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
    }

    @Override
    protected final DependencyAgent createInternal(AgentDatastructure data, Configuration config) {
        if (data.getText() == null || data.getTextState() == null || data.getModelState() == null || data.getRecommendationState() == null) {
            throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
        }
        return this.create(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), config);
    }

    public abstract DependencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
                                                     Configuration config);
}
