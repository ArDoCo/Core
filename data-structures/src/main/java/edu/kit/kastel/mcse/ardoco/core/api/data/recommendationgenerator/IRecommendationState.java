/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator;

import java.util.List;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.IConfigurable;
import edu.kit.kastel.mcse.ardoco.core.common.ICopyable;

/**
 * The Interface IRecommendationState defines the state for recommendations.
 */
public interface IRecommendationState extends ICopyable<IRecommendationState>, IConfigurable {

    /**
     * Returns all recommended instances.
     *
     * @return all recommended instances as list
     */
    ImmutableList<IRecommendedInstance> getRecommendedInstances();

    /**
     * Returns all instance relations.
     *
     * @return all instance relations as list
     */
    ImmutableList<IInstanceRelation> getInstanceRelations();

    void addInstanceRelation(IRecommendedInstance fromInstance, IRecommendedInstance toInstance, IWord relator, List<IWord> from, List<IWord> to);

    /**
     * Adds a recommended instance without a type.
     *
     * @param name         name of that recommended instance
     * @param probability  probability of being in the model
     * @param nameMappings name mappings representing that recommended instance
     */
    void addRecommendedInstance(String name, double probability, ImmutableList<INounMapping> nameMappings);

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
    IRecommendedInstance addRecommendedInstance(String name, String type, double probability, ImmutableList<INounMapping> nameMappings,
            ImmutableList<INounMapping> typeMappings);

    /**
     * Returns all recommended instances that contain a given mapping as type.
     *
     * @param mapping given mapping to search for in types
     * @return the list of recommended instances with the mapping as type.
     */
    ImmutableList<IRecommendedInstance> getRecommendedInstancesByTypeMapping(INounMapping mapping);

    /**
     * Returns all recommended instances that contain a given mapping.
     *
     * @param mapping given mapping to search for
     * @return the list of recommended instances with the mapping.
     */
    ImmutableList<IRecommendedInstance> getAnyRecommendedInstancesByMapping(INounMapping mapping);

    /**
     * Returns all recommended instances that contain a given name.
     *
     * @param name given name to search for in names
     * @return the list of recommended instances with that name.
     */
    ImmutableList<IRecommendedInstance> getRecommendedInstancesByName(String name);

    /**
     * Returns all recommended instances that contain a similar name.
     *
     * @param name given name to search for in names
     * @return the list of recommended instances with a similar name.
     */
    ImmutableList<IRecommendedInstance> getRecommendedInstancesBySimilarName(String name);

    /**
     * Returns all recommended instances that contain a given name and type.
     *
     * @param type given type to search for in types
     * @return the list of recommended instances with that name and type
     */
    ImmutableList<IRecommendedInstance> getRecommendedInstancesByType(String type);

    /**
     * Returns all recommended instances that contain a similar type.
     *
     * @param type given type to search for in types
     * @return the list of recommended instances with a similar type.
     */
    ImmutableList<IRecommendedInstance> getRecommendedInstancesBySimilarType(String type);

}
