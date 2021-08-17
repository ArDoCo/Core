package edu.kit.kastel.mcse.ardoco.core.datastructures.agents;

import edu.kit.kastel.mcse.ardoco.core.datastructures.ICopyable;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IState;

/**
 * The data structure (blackboard) for the {@link Agent Agents}.
 */
public final class AgentDatastructure implements ICopyable<AgentDatastructure> {

    private IText text;
    private ITextState textState;
    private IModelState modelState;
    private IRecommendationState recommendationState;
    private IConnectionState connectionState;
    private IInconsistencyState inconsistencyState;

    /**
     * Create an empty data structure.
     */
    private AgentDatastructure() {
    }

    /**
     * Create a deep copy of the data structure.
     *
     * @return the copy of the data structure
     * @see IState#createCopy()
     */
    @Override
    public AgentDatastructure createCopy() {
        var data = new AgentDatastructure();
        data.text = text;
        data.textState = textState == null ? null : textState.createCopy();
        data.modelState = modelState == null ? null : modelState.createCopy();
        data.recommendationState = recommendationState == null ? null : recommendationState.createCopy();
        data.connectionState = connectionState == null ? null : connectionState.createCopy();
        return data;
    }

    /**
     * Create a new data structure based on states.
     *
     * @param text                the input text
     * @param textState           the text state
     * @param modelState          the model state
     * @param recommendationState the recommendation state
     * @param connectionState     the connection state
     */
    public AgentDatastructure(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState) {
        this.text = text;
        this.textState = textState;
        this.modelState = modelState;
        this.recommendationState = recommendationState;
        this.connectionState = connectionState;
        this.inconsistencyState = inconsistencyState;
    }

    /**
     * Get the text information.
     *
     * @return the text
     */
    public IText getText() {
        return text;
    }

    /**
     * Get the stored text state.
     *
     * @return the text state
     */
    public ITextState getTextState() {
        return textState;
    }

    /**
     * Set the internal text state.
     *
     * @param textState the text state
     */
    public void setTextState(ITextState textState) {
        this.textState = textState;
    }

    /**
     * Get the internal model state.
     *
     * @return the model state
     */
    public IModelState getModelState() {
        return modelState;
    }

    /**
     * Sets the internal model state.
     *
     * @param modelState the new model state
     */
    public void setModelState(IModelState modelState) {
        this.modelState = modelState;
    }

    /**
     * Gets the internal recommendation state.
     *
     * @return the recommendation state
     */
    public IRecommendationState getRecommendationState() {
        return recommendationState;
    }

    /**
     * Sets the internal recommendation state.
     *
     * @param recommendationState the new recommendation state
     */
    public void setRecommendationState(IRecommendationState recommendationState) {
        this.recommendationState = recommendationState;
    }

    /**
     * Gets the internal connection state.
     *
     * @return the connection state
     */
    public IConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * Sets the connection state.
     *
     * @param connectionState the new connection state
     */
    public void setConnectionState(IConnectionState connectionState) {
        this.connectionState = connectionState;
    }

    public IInconsistencyState getInconsistencyState() {
        return inconsistencyState;
    }

    public void setInconsistencyState(IInconsistencyState inconsistencyState) {
        this.inconsistencyState = inconsistencyState;
    }

    /**
     * Override the internal data with data from another data structure.
     *
     * @param newData the new data
     */
    public void overwrite(AgentDatastructure newData) {
        text = newData.text;
        textState = newData.textState;
        modelState = newData.modelState;
        recommendationState = newData.recommendationState;
        connectionState = newData.connectionState;
        inconsistencyState = newData.inconsistencyState;
    }
}
