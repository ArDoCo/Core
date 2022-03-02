/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.common.Agent;
import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

public abstract class InconsistencyAgent extends Agent {

    protected IText text;
    protected ITextState textState;
    protected IModelState modelState;
    protected IRecommendationState recommendationState;
    protected IConnectionState connectionState;
    protected IInconsistencyState inconsistencyState;

    /**
     * Prototype Constructor
     *
     * @param configType the configuration type to be used by the agent
     */
    protected InconsistencyAgent(Class<? extends Configuration> configType) {
        super(configType);
    }

    /**
     * Instantiates a new inconsistency agent.
     *
     * @param configType          the config type
     * @param text                the text
     * @param textState           the text state
     * @param modelState          the model state
     * @param recommendationState the recommendation state
     * @param connectionState     the connection state
     * @param inconsistencyState  the inconsistency state
     */
    protected InconsistencyAgent(Class<? extends Configuration> configType, IText text, ITextState textState, IModelState modelState,
            IRecommendationState recommendationState, IConnectionState connectionState, IInconsistencyState inconsistencyState) {
        super(configType);
        this.text = text;
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
        this.connectionState = connectionState;
        this.inconsistencyState = inconsistencyState;
    }

    @Override
    protected final InconsistencyAgent createInternal(String modelId, AgentDatastructure data, Configuration config) {
        Objects.requireNonNull(data.getText());
        Objects.requireNonNull(data.getTextState());
        Objects.requireNonNull(data.getModelState(modelId));
        Objects.requireNonNull(data.getRecommendationState(modelId));
        Objects.requireNonNull(data.getConnectionState(modelId));
        Objects.requireNonNull(data.getInconsistencyState(modelId));

        return this.create(data.getText(), data.getTextState(), data.getModelState(modelId), data.getRecommendationState(modelId),
                data.getConnectionState(modelId), data.getInconsistencyState(modelId), config);
    }

    public abstract InconsistencyAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState, Configuration config);

}
