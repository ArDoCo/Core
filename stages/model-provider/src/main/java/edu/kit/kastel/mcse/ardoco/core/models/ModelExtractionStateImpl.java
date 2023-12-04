/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.models;

import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;

/**
 * This state contains all from the model extracted information. This are the extracted instances and relations. For easier handling, the occurring types and
 * names are stored additionally.
 */
public class ModelExtractionStateImpl extends AbstractState implements ModelExtractionState {

    private final String modelId;
    private final Metamodel metamodelType;
    private final MutableSortedSet<String> instanceTypes;
    private final MutableSortedSet<String> names;
    private ImmutableList<ModelInstance> instances;

    // For generation of configuration
    private ModelExtractionStateImpl(DataRepository dataRepository) {
        super(dataRepository);
        this.modelId = null;
        this.metamodelType = null;
        this.instanceTypes = null;
        this.names = null;
    }

    /**
     * Creates a new model extraction state.
     *
     * @param dataRepository the {@link DataRepository} this state is associated with
     * @param modelId        the model id
     * @param metamodelType  the metamodel type
     * @param instances      instances of this model extraction state
     */
    public ModelExtractionStateImpl(DataRepository dataRepository, String modelId, Metamodel metamodelType, ImmutableList<ModelInstance> instances) {
        super(dataRepository);
        this.modelId = Objects.requireNonNull(modelId);
        this.metamodelType = metamodelType;
        this.instances = instances;
        instanceTypes = SortedSets.mutable.empty();
        names = SortedSets.mutable.empty();
        collectTypesAndNames();
    }

    /**
     * Collects all occurring types and names from the instances and relations and stores them. The titles of relations are stored in types.
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
    public ImmutableSortedSet<String> getInstanceTypes() {
        return instanceTypes.toImmutable();
    }

    /**
     * Returns all names that are contained by this state.
     *
     * @return all names of this state
     */
    @Override
    public ImmutableSortedSet<String> getNames() {
        return names.toImmutable();
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
        names.addAll(other.getNames().castToCollection());

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
