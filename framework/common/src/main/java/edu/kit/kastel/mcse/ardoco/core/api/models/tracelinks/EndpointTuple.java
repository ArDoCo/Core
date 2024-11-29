/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.io.Serializable;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.entity.Entity;

/**
 * A tuple of one architecture endpoint and one code endpoint. Every endpoint tuple is a possible candidate for the endpoints of a trace link that connects
 * corresponding elements of an architecture model and a code model. An endpoint tuple cannot consist of two architecture endpoints or of two code endpoints.
 */
public class EndpointTuple<E1 extends Entity, E2 extends Entity> implements Serializable {
    private static final long serialVersionUID = -3671983559151147055L;

    private final E1 firstEndpoint;
    private final E2 secondEndpoint;

    /**
     * @param firstEndpoint  the architecture endpoint of the endpoint tuple to be created
     * @param secondEndpoint the code endpoint of the endpoint tuple to be created
     */
    public EndpointTuple(E1 firstEndpoint, E2 secondEndpoint) {
        this.firstEndpoint = firstEndpoint;
        this.secondEndpoint = secondEndpoint;
    }

    public Entity getOtherEndpoint(Entity endpoint) {
        if (this.firstEndpoint.equals(endpoint)) {
            return this.secondEndpoint;
        }
        if (this.secondEndpoint.equals(endpoint)) {
            return this.firstEndpoint;
        }
        throw new IllegalArgumentException("Endpoint tuple must contain given endpoint");
    }

    public boolean hasEndpoint(Entity endpoint) {
        return this.firstEndpoint.equals(endpoint) || this.secondEndpoint.equals(endpoint);
    }

    public boolean hasEndpoint(EndpointTuple<?, ?> endpointTuple) {
        return this.firstEndpoint.equals(endpointTuple.firstEndpoint) || this.secondEndpoint.equals(endpointTuple.secondEndpoint);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EndpointTuple<?, ?> other)) {
            return false;
        }
        return Objects.equals(this.firstEndpoint, other.firstEndpoint) && Objects.equals(this.secondEndpoint, other.secondEndpoint);
    }

    @Override
    public String toString() {
        return "First Endpoint: " + this.firstEndpoint + ", Second Endpoint: " + this.secondEndpoint;
    }

    public E1 firstEndpoint() {
        return this.firstEndpoint;
    }

    public E2 secondEndpoint() {
        return this.secondEndpoint;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.firstEndpoint, this.secondEndpoint);
    }

}
