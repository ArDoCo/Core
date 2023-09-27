/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.Arrays;

/**
 * An entity with a name. Is a model element.
 */
public abstract class Entity extends ModelElement {

    @JsonProperty
    private String name;

    protected Entity() {
        // Jackson
    }

    /**
     * Creates a new entity with the specified name.
     *
     * @param name the name of the entity to be created
     */
    protected Entity(String name) {
        this.name = name;
    }

    protected Entity(String name, String id) {
        super(id);
        this.name = name;
    }

    /**
     * Returns the entity's name.
     *
     * @return the entity's name
     */
    public String getName() {
        return name;
    }

    public ImmutableList<String> getNameParts() {
        return Lists.immutable.ofAll(Arrays.stream(getName().split(" ")).toList());
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Entity entity))
            return false;
        if (!super.equals(o))
            return false;
        return name.equals(entity.name) && getId().equals(entity.getId());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        return 31 * result + getId().hashCode();
    }
}
