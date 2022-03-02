/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.model.provider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.common.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.model.IModelRelation;
import edu.kit.kastel.mcse.ardoco.core.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.model.ModelExtractionState;

/**
 * The model extractor extracts the instances and relations via an connector. The extracted items are stored in a model
 * extraction state.
 *
 * @author Sophie
 *
 */
public final class ModelProvider implements IExecutionStage {

    private IModelState modelExtractionState;
    private IModelConnector modelConnector;

    /**
     * Instantiates a new model provider.
     *
     * @param modelConnector the model connector
     */
    public ModelProvider(IModelConnector modelConnector) {
        this.modelConnector = modelConnector;
    }

    @Override
    public AgentDatastructure getBlackboard() {
        Map<String, IModelState> modelState = new HashMap<>();
        modelState.put(modelExtractionState.getModelId(), modelExtractionState);
        return new AgentDatastructure(null, null, modelState, null, null, null);
    }

    @Override
    public void exec() {
        ImmutableList<IModelInstance> instances = modelConnector.getInstances();
        ImmutableList<IModelRelation> relations = modelConnector.getRelations();

        modelExtractionState = new ModelExtractionState(modelConnector.getModelId(), modelConnector.getMetamodel(), instances, relations);
    }

    @Override
    public IExecutionStage create(AgentDatastructure data, Map<String, String> configs) {
        return new ModelProvider(modelConnector);
    }

}
