/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models;

import java.io.Serializable;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.common.IdentifierProvider;

/**
 * A model element. Has an identifier. Two model elements are equal if and only if they have the same identifier.
 */
public abstract class ModelElement implements Comparable<ModelElement>, Serializable {

    private final String id;

    protected ModelElement() {
        this.id = IdentifierProvider.createId();
    }

    protected ModelElement(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ModelElement other)) {
            return false;
        }
        return Objects.equals(id, other.id);
    }

    @Override
    public int compareTo(ModelElement o) {
        if (this.equals(o))
            return 0;
        return this.id.compareTo(o.id);
    }
}
