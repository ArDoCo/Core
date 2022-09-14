/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.HashSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.agent.Claimant;
import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;

/**
 * The recommendation state encapsulates all recommended instances and relations. These recommendations should be
 * contained by the model by their probability.
 *
 */
public class RecommendationStateImpl extends AbstractState implements RecommendationState {

    private transient MutableList<RecommendedInstance> recommendedInstances;

    /**
     * Creates a new recommendation state.
     */
    public RecommendationStateImpl() {
        recommendedInstances = Lists.mutable.empty();
    }

    /**
     * Returns all recommended instances.
     *
     * @return all recommended instances as list
     */
    @Override
    public ImmutableList<RecommendedInstance> getRecommendedInstances() {
        return Lists.immutable.withAll(recommendedInstances);
    }

    /**
     * Adds a recommended instance without a type.
     *
     * @param name         name of that recommended instance
     * @param probability  probability of being in the model
     * @param nameMappings name mappings representing that recommended instance
     */
    @Override
    public void addRecommendedInstance(String name, Claimant claimant, double probability, ImmutableList<NounMapping> nameMappings) {
        this.addRecommendedInstance(name, "", claimant, probability, nameMappings, Lists.immutable.empty());
    }

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
    @Override
    public RecommendedInstance addRecommendedInstance(String name, String type, Claimant claimant, double probability, ImmutableList<NounMapping> nameMappings,
            ImmutableList<NounMapping> typeMappings) {
        var recommendedInstance = new RecommendedInstanceImpl(name, type, claimant, probability, //
                Lists.immutable.withAll(new HashSet<>(nameMappings.castToCollection())), Lists.immutable.withAll(new HashSet<>(typeMappings
                        .castToCollection())));
        this.addRecommendedInstance(recommendedInstance);

        return recommendedInstance;
    }

    /**
     * Adds a recommended instance to the state. If the in the stored instance an instance with the same name and type
     * is contained it is extended. If an recommendedInstance with the same name can be found it is extended. Elsewhere
     * a new recommended instance is created.
     */
    private void addRecommendedInstance(RecommendedInstance ri) {
        if (recommendedInstances.contains(ri)) {
            return;
        }

        var risWithExactName = recommendedInstances.select(r -> r.getName().equalsIgnoreCase(ri.getName())).toImmutable();
        var risWithExactNameAndType = risWithExactName.select(r -> r.getType().equalsIgnoreCase(ri.getType()));

        if (risWithExactNameAndType.isEmpty()) {
            processRecommendedInstancesWithNoExactNameAndType(ri, risWithExactName);
        } else {
            risWithExactNameAndType.get(0).addMappings(ri.getNameMappings(), ri.getTypeMappings());
        }
    }

    private void processRecommendedInstancesWithNoExactNameAndType(RecommendedInstance ri, ImmutableList<RecommendedInstance> risWithExactName) {
        if (risWithExactName.isEmpty()) {
            recommendedInstances.add(ri);
        } else {
            var added = false;

            for (RecommendedInstance riWithExactName : risWithExactName) {
                var areWordsSimilar = SimilarityUtils.areWordsSimilar(riWithExactName.getType(), ri.getType());
                if (areWordsSimilar || recommendedInstancesHasEmptyType(ri, riWithExactName)) {
                    riWithExactName.addMappings(ri.getNameMappings(), ri.getTypeMappings());
                    added = true;
                    break;
                }
            }

            if (!added && !ri.getType().isBlank()) {
                recommendedInstances.add(ri);
            }
        }
    }

    private static boolean recommendedInstancesHasEmptyType(RecommendedInstance ri, RecommendedInstance riWithExactName) {
        return riWithExactName.getType().isBlank() && !ri.getType().isBlank();
    }

    /**
     * Returns all recommended instances that contain a given mapping as type.
     *
     * @param mapping given mapping to search for in types
     * @return the list of recommended instances with the mapping as type.
     */
    @Override
    public ImmutableList<RecommendedInstance> getRecommendedInstancesByTypeMapping(NounMapping mapping) {
        return recommendedInstances.select(sinstance -> sinstance.getTypeMappings().contains(mapping)).toImmutable();
    }

    /**
     * Returns all recommended instances that contain a given mapping.
     *
     * @param mapping given mapping to search for
     * @return the list of recommended instances with the mapping.
     */
    @Override
    public ImmutableList<RecommendedInstance> getAnyRecommendedInstancesByMapping(NounMapping mapping) {
        return recommendedInstances //
                .select(sinstance -> sinstance.getTypeMappings().contains(mapping) || sinstance.getNameMappings().contains(mapping))
                .toImmutable();
    }

    /**
     * Returns all recommended instances that contain a given name.
     *
     * @param name given name to search for in names
     * @return the list of recommended instances with that name.
     */
    @Override
    public ImmutableList<RecommendedInstance> getRecommendedInstancesByName(String name) {
        return recommendedInstances.select(ri -> ri.getName().toLowerCase().contentEquals(name.toLowerCase())).toImmutable();
    }

    /**
     * Returns all recommended instances that contain a similar name.
     *
     * @param name given name to search for in names
     * @return the list of recommended instances with a similar name.
     */
    @Override
    public ImmutableList<RecommendedInstance> getRecommendedInstancesBySimilarName(String name) {
        MutableList<RecommendedInstance> ris = Lists.mutable.empty();
        for (RecommendedInstance ri : recommendedInstances) {
            if (SimilarityUtils.areWordsSimilar(ri.getName(), name)) {
                ris.add(ri);
            }
        }

        return ris.toImmutable();
    }

    /**
     * Returns all recommended instances that contain a given name and type.
     *
     * @param type given type to search for in types
     * @return the list of recommended instances with that name and type
     */
    @Override
    public ImmutableList<RecommendedInstance> getRecommendedInstancesByType(String type) {
        return recommendedInstances.select(ri -> ri.getType().toLowerCase().contentEquals(type.toLowerCase())).toImmutable();
    }

    /**
     * Returns all recommended instances that contain a similar type.
     *
     * @param type given type to search for in types
     * @return the list of recommended instances with a similar type.
     */
    @Override
    public ImmutableList<RecommendedInstance> getRecommendedInstancesBySimilarType(String type) {
        return recommendedInstances.select(ri -> SimilarityUtils.areWordsSimilar(ri.getType(), type)).toImmutable();
    }
}
