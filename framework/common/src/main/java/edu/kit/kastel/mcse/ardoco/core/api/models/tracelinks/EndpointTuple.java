/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.io.Serializable;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

/**
 * A tuple of one architecture endpoint and one code endpoint. Every endpoint tuple is a possible candidate for the endpoints of a trace link that connects
 * corresponding elements of an architecture model and a code model. An endpoint tuple cannot consist of two architecture endpoints or of two code endpoints.
 */
public class EndpointTuple implements Serializable {
    private final Entity firstEndpoint;
    private final Entity secondEndpoint;

    /**
     * @param firstEndpoint  the architecture endpoint of the endpoint tuple to be created
     * @param secondEndpoint the code endpoint of the endpoint tuple to be created
     */
    public EndpointTuple(Entity firstEndpoint, Entity secondEndpoint) {
        this.firstEndpoint = firstEndpoint;
        this.secondEndpoint = secondEndpoint;
    }

    public Entity getOtherEndpoint(Entity endpoint) {
        if (firstEndpoint.equals(endpoint)) {
            return secondEndpoint;
        }
        if (secondEndpoint.equals(endpoint)) {
            return firstEndpoint;
        }
        throw new IllegalArgumentException("Endpoint tuple must contain given endpoint");
    }

    public boolean hasEndpoint(Entity endpoint) {
        return firstEndpoint.equals(endpoint) || secondEndpoint.equals(endpoint);
    }

    public boolean hasEndpoint(EndpointTuple endpointTuple) {
        return firstEndpoint.equals(endpointTuple.firstEndpoint) || secondEndpoint.equals(endpointTuple.secondEndpoint);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EndpointTuple other)) {
            return false;
        }
        return Objects.equals(firstEndpoint, other.firstEndpoint) && Objects.equals(secondEndpoint, other.secondEndpoint);
    }

    @Override
    public String toString() {
        return "Architecture Endpoint: " + firstEndpoint + ", Code Endpoint: " + secondEndpoint;
    }

    public Entity firstEndpoint() {
        return firstEndpoint;
    }

    public Entity secondEndpoint() {
        return secondEndpoint;
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstEndpoint, secondEndpoint);
    }

}
