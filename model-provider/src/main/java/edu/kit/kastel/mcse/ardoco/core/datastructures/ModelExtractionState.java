package edu.kit.kastel.mcse.ardoco.core.datastructures;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelRelation;

/**
 * This state contains all from the model extracted information. This are the extracted instances and relations. For
 * easier handling, the occurring types and names are stored additionally.
 *
 * @author Sophie
 *
 */
public class ModelExtractionState implements IModelState {

    private Set<String> instanceTypes;
    private Set<String> relationTypes;
    private Set<String> names;
    private List<IModelInstance> instances;
    private List<IModelRelation> relations;

    @Override
    public IModelState createCopy() {
        return new ModelExtractionState(instanceTypes, relationTypes, names, instances.stream().map(IModelInstance::createCopy).collect(Collectors.toList()),
                relations.stream().map(IModelRelation::createCopy).collect(Collectors.toList()));
    }

    private ModelExtractionState(Set<String> instanceTypes, Set<String> relationTypes, Set<String> names, List<IModelInstance> instances,
            List<IModelRelation> relations) {
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
    public ModelExtractionState(List<IModelInstance> instances, List<IModelRelation> relations) {
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
            List<String> typeParts = List.of(r.getType().split(" "));
            if (typeParts.size() >= ModelExtractionStateConfig.EXTRACTION_STATE_MIN_TYPE_PARTS) {
                relationTypes.addAll(typeParts);
            }
        }
        for (IModelInstance i : instances) {
            instanceTypes.addAll(i.getTypes());
            names.addAll(i.getNames());
        }
    }

    /**
     * Returns the instances of a specific type.
     *
     * @param type the type to search for
     * @return all instances that are from that type
     */
    @Override
    public List<IModelInstance> getInstancesOfType(String type) {
        return instances.stream().filter(i -> i.getTypes().contains(type)).collect(Collectors.toList());
    }

    /**
     * Returns the relations of a specific type.
     *
     * @param type the type to search for
     * @return all relations that are from that type
     */
    @Override
    public List<IModelRelation> getRelationsOfType(String type) {
        return relations.stream().filter(r -> r.getType().equals(type)).collect(Collectors.toList());
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
    public List<IModelInstance> getInstances() {
        return instances;
    }

    /**
     * Returns all relations that are contained by this state.
     *
     * @return all relations of this state
     */
    @Override
    public List<IModelRelation> getRelations() {
        return relations;
    }

    @Override
    public String toString() {
        var output = new StringBuilder("Instances:\n");
        for (IModelInstance i : instances) {
            output.append(i.toString() + "\n");
        }
        output.append("Relations:\n");
        for (IModelRelation r : relations) {
            output.append(r.toString() + "\n");
        }
        return output.toString();
    }

}
