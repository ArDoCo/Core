package edu.kit.ipd.consistency_analyzer.modules;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.consistency_analyzer.datastructures.IInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelExtractionState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRelation;
import edu.kit.ipd.consistency_analyzer.modelproviders.IModelConnector;
import edu.kit.ipd.consistency_analyzer.modelproviders.exception.InconsistentModelException;
import edu.kit.ipd.constistency_analyzer.datastructures.ModelExtractionState;

/**
 * The model extractor extracts the instances and relations via an connector.
 * The extracted items are stored in a model extraction state.
 *
 * @author Sophie
 *
 */
public class ModelExtractor implements IModule<IModelExtractionState> {

	protected IModelExtractionState modelExtractionState;
	private IModelConnector modelConnector;

	private List<IInstance> instances = new ArrayList<>();
	private List<IRelation> relations = new ArrayList<>();

	public ModelExtractor(IModelConnector modelConnector) throws InconsistentModelException {
		this.modelConnector = modelConnector;
	}

	@Override
	public IModelExtractionState getState() throws InconsistentModelException {
		return modelExtractionState;
	}

	// TODO: poss. enabling multiple models as input
	@Override
	public void exec() {

		instances = modelConnector.getInstances();
		relations = modelConnector.getRelations(instances);

		modelExtractionState = new ModelExtractionState(instances, relations);

	}

}