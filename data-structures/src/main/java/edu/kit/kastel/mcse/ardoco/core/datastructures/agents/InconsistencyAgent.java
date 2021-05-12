package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;

public abstract class InconsistencyAgent extends Agent {

    protected IText text;
    protected ITextState textState;
    protected IModelState modelState;
    protected IRecommendationState recommendationState;
    protected IConnectionState connectionState;
    protected IInconsistencyState inconsistencyState;

    /**
     * Prototype Constructor
     */
    protected InconsistencyAgent(Class<? extends Configuration> configType) {
        super(configType);
    }

    protected InconsistencyAgent(//
            DependencyType dependencyType, Class<? extends Configuration> configType, IText text, ITextState textState, IModelState modelState,
            IRecommendationState recommendationState, IConnectionState connectionState, IInconsistencyState inconsistencyState) {
        super(dependencyType, configType);
        this.text = text;
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
        this.connectionState = connectionState;
        this.inconsistencyState = inconsistencyState;
    }

    @Override
    protected final InconsistencyAgent createInternal(AgentDatastructure data, Configuration config) {
        if (data.getText() == null || data.getTextState() == null || data.getModelState() == null || data.getRecommendationState() == null
                || data.getConnectionState() == null || data.getInconsistencyState() == null) {
            throw new IllegalArgumentException("An input of the agent" + getName() + " was null!");
        }
        return this.create(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), data.getConnectionState(),
                data.getInconsistencyState(), config);
    }

    public abstract InconsistencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, Configuration config);

}
