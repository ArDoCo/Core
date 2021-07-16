package edu.kit.kastel.mcse.ardoco.core.model.provider;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IModule;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;

/**
 * The model extractor extracts the instances and relations via an connector. The extracted items are stored in a model
 * extraction state.
 *
 * @author Sophie
 *
 */
public final class ModelProvider implements IModule<IModelState> {

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
    public IModelState getState() {
        return modelExtractionState;
    }

    @Override
    public void exec() {
        List<IInstance> instances = modelConnector.getInstances();
        List<IRelation> relations = modelConnector.getRelations();

        modelExtractionState = new ModelExtractionState(instances, relations);

    }

    @Override
    public IModule<IModelState> create(IModelState data, Map<String, String> configs) {
        return new ModelProvider(modelConnector);
    }

}
