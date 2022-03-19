/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.model.provider;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelRelation;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.ModelExtractionState;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Map;

/**
 * The model extractor extracts the instances and relations via an connector. The extracted items are stored in a model
 * extraction state.
 *
 * @author Sophie
 */
public final class ModelProvider {

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

	public IModelState execute(Map<String, String> additionalSettings) {
		ImmutableList<IModelInstance> instances = modelConnector.getInstances();
		ImmutableList<IModelRelation> relations = modelConnector.getRelations();

		return new ModelExtractionState(modelConnector.getModelId(), modelConnector.getMetamodel(), instances, relations, additionalSettings);
	}

}
