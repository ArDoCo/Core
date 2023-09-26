/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.models;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;

/**
 * This state contains all from the model extracted information. This are the extracted instances and relations. For
 * easier handling, the occurring types and names are stored additionally.
 */
public class ModelExtractionStateImpl extends AbstractState implements ModelExtractionState {

    private final String modelId;
    private final Metamodel metamodelType;
    private final MutableSet<String> entityTypes;
    private final MutableSet<String> names;
    private transient ImmutableList<Entity> entities;

    // For generation of configuration
    private ModelExtractionStateImpl() {
        this.modelId = null;
        this.metamodelType = null;
        this.entityTypes = null;
        this.names = null;
    }

    /**
     * Creates a new model extraction state.
     *
     * @param modelId       the model id
     * @param metamodelType the metamodel type
     * @param entities     entities of this model extraction state
     */
    public ModelExtractionStateImpl(String modelId, Metamodel metamodelType, ImmutableList<Entity> entities) {
        this.modelId = Objects.requireNonNull(modelId);
        this.metamodelType = metamodelType;
        this.entities = entities;
        entityTypes = Sets.mutable.empty();
        names = Sets.mutable.empty();
        collectTypesAndNames();
    }

    /**
     * Collects all occurring types and names from the entities and relations and stores them. The titles of relations
     * are stored in types.
     */
    private void collectTypesAndNames() {
        for (Entity i : entities) {
            entityTypes.add(i.getClass().getName());
            names.addAll(Arrays.stream(i.getName().split(" ")).toList());
        }
    }

    @Override
    public String getModelId() {
        return modelId;
    }

    @Override
    public Metamodel getMetamodel() {
        return metamodelType;
    }

    /**
     * Returns the entities of a specific type.
     *
     * @param type the type to search for
     * @return all entities that are from that type
     */
    @Override
    public ImmutableList<Entity> getEntitiesOfType(String type) {
        return entities.select(i -> i.getClass().getName().equals(type));
    }

    /**
     * Returns all types that are contained by entities of this state.
     *
     * @return all entity types of this state
     */
    @Override
    public ImmutableSet<String> getEntityTypes() {
        return entityTypes.toImmutable();
    }

    /**
     * Returns all names that are contained by this state.
     *
     * @return all names of this state
     */
    @Override
    public Set<String> getNames() {
        return names;
    }

    /**
     * Returns all entities that are contained by this state.
     *
     * @return all entities of this state
     */
    @Override
    public ImmutableList<Entity> getEntities() {
        return entities;
    }

    @Override
    public void addAllOf(ModelExtractionState other) {
        entityTypes.addAll(other.getEntityTypes().toSet());
        names.addAll(other.getNames());

        var mergedEntities = Lists.mutable.ofAll(entities);
        mergedEntities.addAll(other.getEntities().toList());
        entities = mergedEntities.toImmutable();
    }

    @Override
    public String toString() {
        var output = new StringBuilder("Entities:\n");
        for (Entity i : entities) {
            output.append(i.toString()).append("\n");
        }
        return output.toString();
    }
}
