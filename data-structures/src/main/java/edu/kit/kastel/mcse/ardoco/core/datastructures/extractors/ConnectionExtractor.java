package edu.kit.kastel.mcse.ardoco.core.datastructures.extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

public abstract class ConnectionExtractor extends Extractor {

    protected ITextState textState;
    protected IModelState modelState;
    protected IRecommendationState recommendationState;
    protected IConnectionState connectionState;

    @Override
    public ConnectionExtractor create(AgentDatastructure data, Configuration config) {

        if (null == data.getTextState() || null == data.getModelState() || null == data.getRecommendationState()) {
            throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
        }
        return create(data.getTextState(), data.getModelState(), data.getRecommendationState(), data.getConnectionState(), config);
    }

    public abstract ConnectionExtractor create(ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, Configuration config);

    protected ConnectionExtractor(DependencyType dependencyType, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState) {
        super(dependencyType);
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
    }
}
