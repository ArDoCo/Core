/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.model;

import java.util.Objects;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;

/**
 * This state contains all from the model extracted information. This are the extracted instances and relations. For
 * easier handling, the occurring types and names are stored additionally.
 *
 */
public class ModelExtractionStateImpl extends AbstractState implements ModelExtractionState {

    private final String modelId;
    private final Metamodel metamodelType;
    private final MutableSet<String> instanceTypes;
    private final MutableSet<String> names;
    private transient ImmutableList<ModelInstance> instances;

    // For generation of configuration
    private ModelExtractionStateImpl() {
        this.modelId = null;
        this.metamodelType = null;
        this.instanceTypes = null;
        this.names = null;
    }

    /**
     * Creates a new model extraction state.
     *
     * @param instances instances of this model extraction state
     */
    public ModelExtractionStateImpl(String modelId, Metamodel metamodelType, ImmutableList<ModelInstance> instances) {
        this.modelId = Objects.requireNonNull(modelId);
        this.metamodelType = metamodelType;
        this.instances = instances;
        instanceTypes = Sets.mutable.empty();
        names = Sets.mutable.empty();
        collectTypesAndNames();
    }

    /**
     * Collects all occurring types and names from the instances and relations and stores them. The titles of relations
     * are stored in types.
     */
    private void collectTypesAndNames() {
        for (ModelInstance i : instances) {
            instanceTypes.addAll(i.getTypeParts().castToCollection());
            names.addAll(i.getNameParts().castToCollection());
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
     * Returns the instances of a specific type.
     *
     * @param type the type to search for
     * @return all instances that are from that type
     */
    @Override
    public ImmutableList<ModelInstance> getInstancesOfType(String type) {
        return instances.select(i -> i.getTypeParts().contains(type));
    }

    /**
     * Returns all types that are contained by instances of this state.
     *
     * @return all instance types of this state
     */
    @Override
    public ImmutableSet<String> getInstanceTypes() {
        return instanceTypes.toImmutable();
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
     * Returns all instances that are contained by this state.
     *
     * @return all instances of this state
     */
    @Override
    public ImmutableList<ModelInstance> getInstances() {
        return instances;
    }

    @Override
    public void addAllOf(ModelExtractionState other) {
        instanceTypes.addAll(other.getInstanceTypes().toSet());
        names.addAll(other.getNames());

        var mergedInstances = Lists.mutable.ofAll(instances);
        mergedInstances.addAll(other.getInstances().toList());
        instances = mergedInstances.toImmutable();
    }

    @Override
    public String toString() {
        var output = new StringBuilder("Instances:\n");
        for (ModelInstance i : instances) {
            output.append(i.toString()).append("\n");
        }
        return output.toString();
    }
}
