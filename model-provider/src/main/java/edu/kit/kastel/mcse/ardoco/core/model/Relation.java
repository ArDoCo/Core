/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.model;

import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

/**
 * Represents a relation extracted from a model. A relation must have at least two end points. These end points are
 * defined as instances.
 *
 * @author Sophie
 *
 */
public class Relation implements IModelRelation {

    private MutableList<IModelInstance> instances;
    private String type;
    private String uid;

    @Override
    public IModelRelation createCopy() {
        return new Relation(Lists.immutable.withAll(instances), type, uid);
    }

    private Relation(ImmutableList<IModelInstance> instances, String type, String uid) {
        this.instances = instances.toList();
        this.type = type;
        this.uid = uid;
    }

    /**
     * Creates a new relation.
     *
     * @param instance1 first instance
     * @param instance2 second instance
     * @param type      title of relation
     * @param uid       unique identifier for trace linking
     */
    public Relation(IModelInstance instance1, IModelInstance instance2, String type, String uid) {
        instances = Lists.mutable.with(instance1, instance2);
        this.type = type;
        this.uid = uid;
    }

    /**
     * Adds more end points to the relation. Checks if the instance is already contained.
     *
     * @param others list of other end points of this relation
     */
    @Override
    public void addOtherInstances(ImmutableList<IModelInstance> others) {
        for (IModelInstance o : others) {
            if (!instances.contains(o)) {
                instances.add(o);
            }
        }
    }

    /**
     * Returns the end points of this relation as instances.
     *
     * @return list of connected instances by this relation
     */
    @Override
    public ImmutableList<IModelInstance> getInstances() {
        return instances.toImmutable();
    }

    /**
     * Returns the determiner of the relation
     *
     * @return the type of relation
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * Returns the unique identifier of this relation.
     *
     * @return the uid of this relation
     */
    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public String toString() {
        MutableList<String> instanceNames = instances.collect(IModelInstance::getFullName);
        return "Relation: [" + " name=" + type + ", instances= " + String.join(", ", instanceNames) + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(instances, type, uid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Relation other = (Relation) obj;
        return Objects.equals(instances, other.instances) && Objects.equals(type, other.type) && Objects.equals(uid, other.uid);
    }

}
