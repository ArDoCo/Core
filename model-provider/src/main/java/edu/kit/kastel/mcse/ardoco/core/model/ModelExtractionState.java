/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.common.Configurable;
import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelRelation;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;

/**
 * This state contains all from the model extracted information. This are the extracted instances and relations. For
 * easier handling, the occurring types and names are stored additionally.
 *
 * @author Sophie
 */
public class ModelExtractionState extends AbstractState implements IModelState {

    private final String modelId;
    private final Metamodel metamodelType;
    private final Set<String> instanceTypes;
    private final Set<String> relationTypes;
    private final Set<String> names;
    private ImmutableList<IModelInstance> instances;
    private ImmutableList<IModelRelation> relations;

    @Configurable
    private int minTypeParts = 2;

    @Override
    public IModelState createCopy() {
        return new ModelExtractionState(modelId, metamodelType, instanceTypes, relationTypes, names, //
                instances.collect(IModelInstance::createCopy), //
                relations.collect(IModelRelation::createCopy), this.configs);
    }

    private ModelExtractionState(String modelId, Metamodel metamodelType, Set<String> instanceTypes, Set<String> relationTypes, Set<String> names,
            ImmutableList<IModelInstance> instances, ImmutableList<IModelRelation> relations, Map<String, String> configs) {
        super(configs);
        this.modelId = modelId;
        this.metamodelType = metamodelType;
        this.instanceTypes = instanceTypes;
        this.relationTypes = relationTypes;
        this.relations = relations;
        this.instances = instances;
        this.names = names;

    }

    /**
     * Creates a new model extraction state.
     *
     * @param instances instances of this model extraction state
     * @param relations relations of this model extraction state
     */
    public ModelExtractionState(String modelId, Metamodel metamodelType, ImmutableList<IModelInstance> instances, ImmutableList<IModelRelation> relations,
            Map<String, String> configs) {
        super(configs);
        this.modelId = Objects.requireNonNull(modelId);
        this.metamodelType = metamodelType;
        this.instances = instances;
        this.relations = relations;
        instanceTypes = new HashSet<>();
        relationTypes = new HashSet<>();
        names = new HashSet<>();
        collectTypesAndNames();
    }

    /**
     * Collects all occurring types and names from the instances and relations and stores them. The titles of relations
     * are stored in types.
     */
    private void collectTypesAndNames() {
        for (IModelRelation r : relations) {
            relationTypes.add(r.getType());
            ImmutableList<String> typeParts = Lists.immutable.with(r.getType().split(" "));
            if (typeParts.size() >= minTypeParts) {
                relationTypes.addAll(typeParts.castToCollection());
            }
        }
        for (IModelInstance i : instances) {
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
    public ImmutableList<IModelInstance> getInstancesOfType(String type) {
        return instances.select(i -> i.getTypeParts().contains(type));
    }

    /**
     * Returns the relations of a specific type.
     *
     * @param type the type to search for
     * @return all relations that are from that type
     */
    @Override
    public ImmutableList<IModelRelation> getRelationsOfType(String type) {
        return relations.select(r -> r.getType().equals(type));
    }

    /**
     * Returns all types that are contained by instances of this state.
     *
     * @return all instance types of this state
     */
    @Override
    public Set<String> getInstanceTypes() {
        return instanceTypes;
    }

    /**
     * Returns all types that are contained by relations of this state.
     *
     * @return all relation types of this state
     */
    @Override
    public Set<String> getRelationTypes() {
        return relationTypes;
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
    public ImmutableList<IModelInstance> getInstances() {
        return instances;
    }

    /**
     * Returns all relations that are contained by this state.
     *
     * @return all relations of this state
     */
    @Override
    public ImmutableList<IModelRelation> getRelations() {
        return relations;
    }

    @Override
    public void addAllOf(IModelState other) {
        instanceTypes.addAll(other.getInstanceTypes());
        relationTypes.addAll(other.getRelationTypes());
        names.addAll(other.getNames());

        var mergedInstances = Lists.mutable.ofAll(instances);
        mergedInstances.addAll(other.getInstances().toList());
        instances = mergedInstances.toImmutable();

        var mergedRelations = Lists.mutable.ofAll(relations);
        mergedRelations.addAll(other.getRelations().toList());
        relations = mergedRelations.toImmutable();
    }

    @Override
    public String toString() {
        var output = new StringBuilder("Instances:\n");
        for (IModelInstance i : instances) {
            output.append(i.toString()).append("\n");
        }
        output.append("Relations:\n");
        for (IModelRelation r : relations) {
            output.append(r.toString()).append("\n");
        }
        return output.toString();
    }

}
