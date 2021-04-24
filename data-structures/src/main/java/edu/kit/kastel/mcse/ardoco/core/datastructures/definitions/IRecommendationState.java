package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.datastructures.modules.IState;

public interface IRecommendationState extends IState {

    IRecommendationState createCopy();

    /**
     * Returns all recommended instances.
     *
     * @return all recommended instances as list
     */
    List<IRecommendedInstance> getRecommendedInstances();

    /**
     * Returns all recommended relations.
     *
     * @return all recommended relations as list
     */
    List<IRecommendedRelation> getRecommendedRelations();

    /**
     * Adds a new recommended relation.
     *
     * @param name           name of that recommended relation
     * @param probability    probability of being in the model
     * @param ri1            first end point of the relation as recommended instance
     * @param ri2            second end point of the relation as recommended instance
     * @param otherInstances other involved recommended instances
     * @param occurrences    nodes representing the relation
     */
    void addRecommendedRelation(String name, IRecommendedInstance ri1, IRecommendedInstance ri2, List<IRecommendedInstance> otherInstances, double probability,
            List<IWord> occurrences);

    /**
     * Adds a recommended instance without a type.
     *
     * @param name         name of that recommended instance
     * @param probability  probability of being in the model
     * @param nameMappings name mappings representing that recommended instance
     */
    void addRecommendedInstanceJustName(String name, double probability, List<INounMapping> nameMappings);

    /**
     * Adds a recommended instance.
     *
     * @param name         name of that recommended instance
     * @param type         type of that recommended instance
     * @param probability  probability of being in the model
     * @param nameMappings name mappings representing the name of the recommended instance
     * @param typeMappings type mappings representing the type of the recommended instance
     * @return the added recommended instance
     */
    IRecommendedInstance addRecommendedInstance(String name, String type, double probability, List<INounMapping> nameMappings, List<INounMapping> typeMappings);

    /**
     * Returns all recommended instances that contain a given mapping as type.
     *
     * @param mapping given mapping to search for in types
     * @return the list of recommended instances with the mapping as type.
     */
    List<IRecommendedInstance> getRecommendedInstancesByTypeMapping(INounMapping mapping);

    /**
     * Returns all recommended instances that contain a given mapping.
     *
     * @param mapping given mapping to search for
     * @return the list of recommended instances with the mapping.
     */
    List<IRecommendedInstance> getAnyRecommendedInstancesByMapping(INounMapping mapping);

    /**
     * Returns all recommended instances that contain a given name.
     *
     * @param name given name to search for in names
     * @return the list of recommended instances with that name.
     */
    List<IRecommendedInstance> getRecommendedInstancesByName(String name);

    /**
     * Returns all recommended instances that contain a similar name.
     *
     * @param name given name to search for in names
     * @return the list of recommended instances with a similar name.
     */
    List<IRecommendedInstance> getRecommendedInstancesBySimilarName(String name);

    /**
     * Returns all recommended instances that contain a given name and type.
     *
     * @param type given type to search for in types
     * @return the list of recommended instances with that name and type
     */
    List<IRecommendedInstance> getRecommendedInstancesByType(String type);

    /**
     * Returns all recommended instances that contain a similar type.
     *
     * @param type given type to search for in types
     * @return the list of recommended instances with a similar type.
     */
    List<IRecommendedInstance> getRecommendedInstancesBySimilarType(String type);

}
