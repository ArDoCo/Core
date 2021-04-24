package edu.kit.kastel.mcse.ardoco.core.model;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRelation;
import edu.kit.kastel.mcse.ardoco.core.model.exception.InconsistentModelException;

public interface IModelConnector {

	// TODO: Model as Input?

	List<IInstance> getInstances();

	List<IRelation> getRelations(List<IInstance> instances) throws InconsistentModelException;

}
