/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;


import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.textextraction.INounMapping;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

/**
 * The recommendation state encapsulates all recommended instances and relations. These recommendations should be
 * contained by the model by their probability.
 *
 * @author Sophie
 *
 */
public class RecommendationState implements IRecommendationState {

    private MutableList<IRecommendedInstance> recommendedInstances;
    private MutableList<IRecommendedRelation> recommendedRelations;
    private MutableList<IInstanceRelation> instanceRelations;

    @Override
    public IRecommendationState createCopy() {
        var recommendationState = new RecommendationState();
        recommendationState.recommendedInstances = recommendedInstances.collect(IRecommendedInstance::createCopy);
        recommendationState.recommendedRelations = recommendedRelations.collect(IRecommendedRelation::createCopy);
        recommendationState.instanceRelations = instanceRelations.collect(IInstanceRelation::createCopy);
        return recommendationState;
    }

    /**
     * Creates a new recommendation state.
     */
    public RecommendationState() {
        recommendedInstances = Lists.mutable.empty();
        recommendedRelations = Lists.mutable.empty();
        instanceRelations = Lists.mutable.empty();
    }

    /**
     * Returns all recommended instances.
     *
     * @return all recommended instances as list
     */
    @Override
    public ImmutableList<IRecommendedInstance> getRecommendedInstances() {
        return Lists.immutable.withAll(recommendedInstances);
    }

    /**
     * Returns all recommended relations.
     *
     * @return all recommended relations as list
     */
    @Override
    public ImmutableList<IRecommendedRelation> getRecommendedRelations() {
        return recommendedRelations.toImmutable();
    }

    /**
     * Returns all instance relations.
     *
     * @return all instance relations as list
     */
    @Override
    public ImmutableList<IInstanceRelation> getInstanceRelations() {
        return instanceRelations.toImmutable();
    }

    /**
     * Adds a new instance relation.
     *
     * @param fromInstance source instances of the instance relation
     * @param toInstance   target instances of the instance relation
     * @param relator      relating word
     * @param from         source nodes of the instance relation
     * @param to           target nodes of the instance relation
     */
    @Override
    public void addInstanceRelation(IRecommendedInstance fromInstance, IRecommendedInstance toInstance, IWord relator, List<IWord> from, List<IWord> to) {
        instanceRelations.add(new InstanceRelation(fromInstance, toInstance, relator, from, to));
    }

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
    @Override
    public void addRecommendedRelation(String name, IRecommendedInstance ri1, IRecommendedInstance ri2, ImmutableList<IRecommendedInstance> otherInstances,
            double probability, ImmutableList<IWord> occurrences) {

        IRecommendedRelation rrel = new RecommendedRelation(name, ri1, ri2, otherInstances, probability, occurrences);
        MutableList<IRecommendedInstance> ris = Lists.mutable.with(ri1, ri2);
        ris.addAll(otherInstances.castToCollection());
        Optional<IRecommendedRelation> recr = recommendedRelations.stream()
                .filter(//
                        r -> r.getRelationInstances().containsAll(ris) && ris.containsAll(r.getRelationInstances().castToCollection()))
                .findAny();

        if (recr.isPresent()) {
            updateRecommendedRelation(recr.get(), occurrences, probability);
        } else {
            recommendedRelations.add(rrel);
        }
    }

    private static void updateRecommendedRelation(IRecommendedRelation recr, ImmutableList<IWord> occurrences, double probability) {
        recr.addOccurrences(occurrences);
        recr.updateProbability(probability);
    }

    /**
     * Adds a recommended instance without a type.
     *
     * @param name         name of that recommended instance
     * @param probability  probability of being in the model
     * @param nameMappings name mappings representing that recommended instance
     */
    @Override
    public void addRecommendedInstanceJustName(String name, double probability, ImmutableList<INounMapping> nameMappings) {
        this.addRecommendedInstance(name, "", probability, nameMappings, Lists.immutable.empty());
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
    public IRecommendedInstance addRecommendedInstance(String name, String type, double probability, ImmutableList<INounMapping> nameMappings,
            ImmutableList<INounMapping> typeMappings) {
        var recommendedInstance = new RecommendedInstance(name, type, probability, //
                Lists.immutable.withAll(new HashSet<>(nameMappings.castToCollection())),
                Lists.immutable.withAll(new HashSet<>(typeMappings.castToCollection())));
        this.addRecommendedInstance(recommendedInstance);

        return recommendedInstance;
    }

    /**
     * Adds a recommended instance to the state. If the in the stored instance an instance with the same name and type
     * is contained it is extended. If an recommendedInstance with the same name can be found it is extended. Elsewhere
     * a new recommended instance is created.
     *
     * @param ri
     */
    private void addRecommendedInstance(IRecommendedInstance ri) {
        if (recommendedInstances.contains(ri)) {
            return;
        }

        ImmutableList<IRecommendedInstance> risWithExactName = recommendedInstances.select(r -> r.getName().equalsIgnoreCase(ri.getName())).toImmutable();
        ImmutableList<IRecommendedInstance> risWithExactNameAndType = risWithExactName.select(r -> r.getType().equalsIgnoreCase(ri.getType()));

        if (risWithExactNameAndType.isEmpty()) {
            processRecommendedInstancesWithNoExactNameAndType(ri, risWithExactName);
        } else {
            risWithExactNameAndType.get(0).addMappings(ri.getNameMappings(), ri.getTypeMappings());

        }

    }

    private void processRecommendedInstancesWithNoExactNameAndType(IRecommendedInstance ri, ImmutableList<IRecommendedInstance> risWithExactName) {
        if (risWithExactName.isEmpty()) {
            recommendedInstances.add(ri);
        } else {
            var added = false;

            for (IRecommendedInstance riWithExactName : risWithExactName) {
                boolean areWordsSimilar = SimilarityUtils.areWordsSimilar(riWithExactName.getType(), ri.getType(), 0.85);
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

    private static boolean recommendedInstancesHasEmptyType(IRecommendedInstance ri, IRecommendedInstance riWithExactName) {
        return riWithExactName.getType().isBlank() && !ri.getType().isBlank();
    }

    /**
     * Returns all recommended instances that contain a given mapping as type.
     *
     * @param mapping given mapping to search for in types
     * @return the list of recommended instances with the mapping as type.
     */
    @Override
    public ImmutableList<IRecommendedInstance> getRecommendedInstancesByTypeMapping(INounMapping mapping) {
        return recommendedInstances.select(sinstance -> sinstance.getTypeMappings().contains(mapping)).toImmutable();
    }

    /**
     * Returns all recommended instances that contain a given mapping.
     *
     * @param mapping given mapping to search for
     * @return the list of recommended instances with the mapping.
     */
    @Override
    public ImmutableList<IRecommendedInstance> getAnyRecommendedInstancesByMapping(INounMapping mapping) {
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
    public ImmutableList<IRecommendedInstance> getRecommendedInstancesByName(String name) {
        return recommendedInstances.select(ri -> ri.getName().toLowerCase().contentEquals(name.toLowerCase())).toImmutable();
    }

    /**
     * Returns all recommended instances that contain a similar name.
     *
     * @param name given name to search for in names
     * @return the list of recommended instances with a similar name.
     */
    @Override
    public ImmutableList<IRecommendedInstance> getRecommendedInstancesBySimilarName(String name) {
        MutableList<IRecommendedInstance> ris = Lists.mutable.empty();
        for (IRecommendedInstance ri : recommendedInstances) {
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
    public ImmutableList<IRecommendedInstance> getRecommendedInstancesByType(String type) {
        return recommendedInstances.select(ri -> ri.getType().toLowerCase().contentEquals(type.toLowerCase())).toImmutable();
    }

    /**
     * Returns all recommended instances that contain a similar type.
     *
     * @param type given type to search for in types
     * @return the list of recommended instances with a similar type.
     */
    @Override
    public ImmutableList<IRecommendedInstance> getRecommendedInstancesBySimilarType(String type) {
        return recommendedInstances.select(ri -> SimilarityUtils.areWordsSimilar(ri.getType(), type)).toImmutable();
    }

}
