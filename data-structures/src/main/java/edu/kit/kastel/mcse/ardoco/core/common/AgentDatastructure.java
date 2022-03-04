/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.common;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.text.IText;
import edu.kit.kastel.mcse.ardoco.core.textextraction.ITextState;

/**
 * The data structure (blackboard) for the {@link Agent Agents}.
 */
public final class AgentDatastructure implements ICopyable<AgentDatastructure> {

    private IText text;
    private ITextState textState;
    private Map<String, IModelState> modelStates;
    private Map<Metamodel, IRecommendationState> recommendationStates;
    private Map<String, IConnectionState> connectionStates;
    private Map<String, IInconsistencyState> inconsistencyStates;

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
        data.modelStates = modelStates == null ? null : copyMap(modelStates, IModelState::createCopy);
        data.recommendationStates = recommendationStates == null ? null : copyMap(recommendationStates, IRecommendationState::createCopy);
        data.connectionStates = connectionStates == null ? null : copyMap(connectionStates, IConnectionState::createCopy);
        return data;
    }

    /**
     * Create a new data structure based on states.
     *
     * @param text                 the input text
     * @param textState            the text states
     * @param modelStates          the model states
     * @param recommendationStates the recommendation states
     * @param connectionStates     the connection state
     */
    public AgentDatastructure(IText text, ITextState textState, Map<String, IModelState> modelStates, Map<Metamodel, IRecommendationState> recommendationStates,
            Map<String, IConnectionState> connectionStates, Map<String, IInconsistencyState> inconsistencyStates) {
        this.text = text;
        this.textState = textState;
        this.modelStates = modelStates;
        this.recommendationStates = recommendationStates;
        this.connectionStates = connectionStates;
        this.inconsistencyStates = inconsistencyStates;
    }

    public AgentDatastructure(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            IConnectionState connectionState, IInconsistencyState inconsistencyState) {
        this.text = text;
        this.textState = textState;
        var modelId = modelState.getModelId();
        this.modelStates = new HashMap<>();
        modelStates.put(modelId, modelState);
        this.recommendationStates = new EnumMap<>(Metamodel.class);
        recommendationStates.put(modelState.getMetamodel(), recommendationState);
        this.connectionStates = new HashMap<>();
        connectionStates.put(modelId, connectionState);
        this.inconsistencyStates = new HashMap<>();
        inconsistencyStates.put(modelId, inconsistencyState);
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

    public List<String> getModelIds() {
        return new ArrayList<>(modelStates.keySet());
    }

    public List<Metamodel> getMetamodelTypes() {
        return new ArrayList<>(this.modelStates.values().stream().map(IModelState::getMetamodel).collect(Collectors.toSet()));
    }

    /**
     * Get the internal model state.
     *
     * @return the model state
     */
    public IModelState getModelState(String modelId) {
        return modelStates.get(modelId);
    }

    /**
     * Sets the internal model state.
     *
     * @param modelState the new model state
     */
    public void addModelState(String modelId, IModelState modelState) {
        this.modelStates.put(modelId, modelState);
    }

    public IRecommendationState getRecommendationState(String modelId) {
        return recommendationStates.get(modelStates.get(modelId).getMetamodel());
    }

    /**
     * Gets the internal recommendation state.
     *
     * @return the recommendation state
     */
    public IRecommendationState getRecommendationState(Metamodel metamodel) {
        return recommendationStates.get(metamodel);
    }

    /**
     * Sets the internal recommendation state.
     *
     * @param recommendationState the new recommendation state
     */
    public void setRecommendationState(Metamodel metamodel, IRecommendationState recommendationState) {
        this.recommendationStates.put(metamodel, recommendationState);
    }

    /**
     * Gets the internal connection state.
     *
     * @return the connection state
     */
    public IConnectionState getConnectionState(String modelId) {
        return connectionStates.get(modelId);
    }

    /**
     * Sets the connection state.
     *
     * @param connectionState the new connection state
     */
    public void setConnectionState(String modelId, IConnectionState connectionState) {
        this.connectionStates.put(modelId, connectionState);
    }

    /**
     * Returns a map with Model-IDs as keys and the corresponding {@link IConnectionState IConnectionStates} as values.
     *
     * @return the IConnectionStates
     */
    public Map<String, IConnectionState> getAllConnectionStates() {
        return connectionStates;
    }

    public IInconsistencyState getInconsistencyState(String modelId) {
        return inconsistencyStates.get(modelId);
    }

    public void setInconsistencyState(String modelId, IInconsistencyState inconsistencyState) {
        this.inconsistencyStates.put(modelId, inconsistencyState);
    }

    /**
     * Override the internal data with data from another data structure.
     *
     * @param newData the new data
     */
    public void overwrite(AgentDatastructure newData) {
        text = newData.text;
        textState = newData.textState;
        modelStates = newData.modelStates;
        recommendationStates = newData.recommendationStates;
        connectionStates = newData.connectionStates;
        inconsistencyStates = newData.inconsistencyStates;
    }

    public <K, V> Map<K, V> copyMap(Map<K, V> map, UnaryOperator<V> copy) {
        Map<K, V> copyMap = new HashMap<>();
        for (var entry : map.entrySet()) {
            copyMap.put(entry.getKey(), copy.apply(entry.getValue()));
        }
        return copyMap;
    }

}
