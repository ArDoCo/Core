package edu.kit.kastel.mcse.ardoco.core.models;

import java.util.Objects;
import java.util.UUID;

/**
 * A model element. Has an identifier. Two model elements are equal if and only if they have the same identifier.
 */
public abstract class ModelElement {

    private final String id;

    public ModelElement() {
        this.id = UUID.randomUUID().toString();
    }

    public ModelElement(String id) {
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
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ModelElement other = (ModelElement) obj;
        return Objects.equals(id, other.id);
    }
}
