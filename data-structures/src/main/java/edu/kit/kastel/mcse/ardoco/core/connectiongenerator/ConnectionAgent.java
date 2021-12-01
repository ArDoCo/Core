/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.connectiongenerator;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.common.Agent;
import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.Configuration;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

/**
 * The base class for connection agents.
 */
public abstract class ConnectionAgent extends Agent {

    /** The text internal text. */
    protected IText text;

    /** The internal text state. */
    protected ITextState textState;

    /** The internal model state. */
    protected IModelState modelState;

    /** The internal recommendation state. */
    protected IRecommendationState recommendationState;

    /** The internal connection state. */
    protected IConnectionState connectionState;

    /**
     * Prototype Constructor.
     *
     * @param configType the type of configuration for this agent type
     */
    protected ConnectionAgent(Class<? extends Configuration> configType) {
        super(configType);
    }

    /**
     * Instantiates a new connection agent.
     *
     * @param configType          the config type
     * @param text                the text
     * @param textState           the text state
     * @param modelState          the model state
     * @param recommendationState the recommendation state
     * @param connectionState     the connection state
     */
    protected ConnectionAgent(Class<? extends Configuration> configType, IText text, ITextState textState, IModelState modelState,
            IRecommendationState recommendationState, IConnectionState connectionState) {
        super(configType);
        this.text = text;
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
        this.connectionState = connectionState;
    }

    @Override
    protected final ConnectionAgent createInternal(AgentDatastructure data, Configuration config) {
        Objects.requireNonNull(data.getText());
        Objects.requireNonNull(data.getTextState());
        Objects.requireNonNull(data.getModelState());
        Objects.requireNonNull(data.getRecommendationState());
        Objects.requireNonNull(data.getConnectionState());

        return this.create(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState(), data.getConnectionState(), config);
    }

    /**
     * Creates the agent.
     *
     * @param text                the text to use
     * @param textState           the text state to use
     * @param modelState          the model state to use
     * @param recommendationState the recommendation state to use
     * @param connectionState     the connection state to use
     * @param config              the config to use
     * @return the connection agent
     */
    public abstract ConnectionAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, Configuration config);

}
