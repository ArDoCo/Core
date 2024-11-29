/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.io.Serializable;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

public abstract class TraceLink<E1 extends Entity, E2 extends Entity> implements Serializable {
    private final E1 endpoint1;
    private final E2 endpoint2;

    protected TraceLink(E1 firstEndpoint, E2 secondEndpoint) {
        this.endpoint1 = firstEndpoint;
        this.endpoint2 = secondEndpoint;
    }

    /**
     * Returns the endpoint tuple of this trace link.
     *
     * @return the endpoint tuple of this trace link
     */
    public EndpointTuple<E1, E2> getEndpointTuple() {
        return new EndpointTuple<>(endpoint1, endpoint2);
    }

    public E1 getFirstEndpoint() {
        return endpoint1;
    }

    public E2 getSecondEndpoint() {
        return endpoint2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint1, endpoint2);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TraceLink<?, ?> other)) {
            return false;
        }
        return Objects.equals(endpoint1, other.endpoint1) && Objects.equals(endpoint2, other.endpoint2);
    }

    @Override
    public String toString() {
        return getEndpointTuple().toString();
    }
}
