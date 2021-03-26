package edu.kit.ipd.consistency_analyzer.modules;

import java.util.List;
import java.util.Map;

import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRelation;
import edu.kit.ipd.consistency_analyzer.modelproviders.IModelConnector;
import edu.kit.ipd.consistency_analyzer.modelproviders.exception.InconsistentModelException;
import edu.kit.ipd.constistency_analyzer.datastructures.ModelExtractionState;

/**
 * The model extractor extracts the instances and relations via an connector. The extracted items are stored in a model
 * extraction state.
 *
 * @author Sophie
 *
 */
public class ModelExtractor implements IModule<IModelState> {

    protected IModelState modelExtractionState;
    private IModelConnector modelConnector;

    public ModelExtractor(IModelConnector modelConnector) throws InconsistentModelException {
        this.modelConnector = modelConnector;
    }

    @Override
    public IModelState getState() throws InconsistentModelException {
        return modelExtractionState;
    }

    // TODO: poss. enabling multiple models as input
    @Override
    public void exec() {

        List<IInstance> instances = modelConnector.getInstances();
        List<IRelation> relations = modelConnector.getRelations(instances);

        modelExtractionState = new ModelExtractionState(instances, relations);

    }

    @Override
    public IModule<IModelState> create(IModelState data, Map<String, String> configs) {
        return new ModelExtractor(modelConnector);
    }

}
