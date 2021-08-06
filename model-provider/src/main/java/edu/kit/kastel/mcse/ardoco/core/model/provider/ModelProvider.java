package edu.kit.kastel.mcse.ardoco.core.model.provider;

import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.datastructures.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IExecutionStage;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;

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
        return new AgentDatastructure(null, null, modelExtractionState, null, null, null);
    }

    @Override
    public void exec() {
        ImmutableList<IModelInstance> instances = modelConnector.getInstances();
        ImmutableList<IModelRelation> relations = modelConnector.getRelations();

        modelExtractionState = new ModelExtractionState(instances, relations);
    }

    @Override
    public IExecutionStage create(AgentDatastructure data, Map<String, String> configs) {
        // TODO Sophie: Check whether it is ok that we do not use the data :)
        return new ModelProvider(modelConnector);
    }

}
