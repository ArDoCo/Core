/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import static edu.kit.kastel.informalin.framework.common.JavaUtils.copyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.agent.ConnectionAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.agent.InconsistencyAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.agent.TextAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.IConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IText;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.ITextState;

public final class DataStructure implements IData, IModelData, ITextData, IRecommendationData, IConnectionData, IInconsistencyData, TextAgentData,
        RecommendationAgentData, ConnectionAgentData, InconsistencyAgentData {
    private final IText text;
    private final Map<String, IModelState> modelStates;

    private ITextState textState;
    private Map<Metamodel, IRecommendationState> recommendationStates = new HashMap<>();
    private Map<String, IConnectionState> connectionStates = new HashMap<>();
    private Map<String, IInconsistencyState> inconsistencyStates = new HashMap<>();

    public DataStructure(IText text, Map<String, IModelState> models) {
        this.text = text;
        this.modelStates = models;
    }

    @Override
    public DataStructure createCopy() {
        DataStructure ds = new DataStructure(text, copyMap(modelStates, IModelState::createCopy));
        ds.textState = textState == null ? null : textState.createCopy();
        ds.recommendationStates = copyMap(recommendationStates, IRecommendationState::createCopy);
        ds.connectionStates = copyMap(connectionStates, IConnectionState::createCopy);
        ds.inconsistencyStates = copyMap(inconsistencyStates, IInconsistencyState::createCopy);
        return ds;
    }

    @Override
    public void setConnectionState(String model, IConnectionState state) {
        ensureModel(model);
        connectionStates.put(model, state);
    }

    @Override
    public IConnectionState getConnectionState(String model) {
        return connectionStates.get(model);
    }

    @Override
    public void setInconsistencyState(String model, IInconsistencyState state) {
        ensureModel(model);
        this.inconsistencyStates.put(model, state);
    }

    @Override
    public IInconsistencyState getInconsistencyState(String model) {
        return inconsistencyStates.get(model);
    }

    @Override
    public List<String> getModelIds() {
        return modelStates.keySet().stream().toList();
    }

    @Override
    public IModelState getModelState(String model) {
        return modelStates.get(model);
    }

    @Override
    public void setRecommendationState(Metamodel mm, IRecommendationState state) {
        Objects.requireNonNull(mm);
        this.recommendationStates.put(mm, state);
    }

    @Override
    public IRecommendationState getRecommendationState(Metamodel mm) {
        return recommendationStates.get(mm);
    }

    @Override
    public IText getText() {
        return text;
    }

    @Override
    public void setTextState(ITextState state) {
        this.textState = state;
    }

    @Override
    public ITextState getTextState() {
        return textState;
    }

    private void ensureModel(String model) {
        if (!this.modelStates.containsKey(model))
            throw new IllegalArgumentException("Model with Key " + model + " was not found");
    }

}
