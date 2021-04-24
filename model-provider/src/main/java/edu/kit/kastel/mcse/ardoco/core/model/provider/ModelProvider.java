package edu.kit.kastel.mcse.ardoco.core.model.provider;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IModule;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.exception.InconsistentModelException;

/**
 * The model extractor extracts the instances and relations via an connector.
 * The extracted items are stored in a model extraction state.
 *
 * @author Sophie
 *
 */
public class ModelProvider implements IModule<IModelState> {

	protected IModelState modelExtractionState;
	private IModelConnector modelConnector;

	public ModelProvider(IModelConnector modelConnector) throws InconsistentModelException {
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
		return new ModelProvider(modelConnector);
	}

}
