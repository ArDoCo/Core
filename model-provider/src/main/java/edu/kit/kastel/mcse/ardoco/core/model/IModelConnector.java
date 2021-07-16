package edu.kit.kastel.mcse.ardoco.core.model;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelRelation;

/**
 * The Interface IModelConnector defines the connection to a computational model.
 */
public interface IModelConnector {

    /**
     * Gets the instances of the model.
     *
     * @return the instances
     */
    List<IModelInstance> getInstances();

    /**
     * Gets the relations from the model.
     *
     * @return the relations
     */
    List<IModelRelation> getRelations();

}
