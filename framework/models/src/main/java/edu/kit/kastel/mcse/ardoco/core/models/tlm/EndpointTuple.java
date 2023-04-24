package edu.kit.kastel.mcse.ardoco.core.models.tlm;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.models.amtl.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.models.cmtl.CodeCompilationUnit;

/**
 * A tuple of one architecture endpoint and one code endpoint. Every endpoint
 * tuple is a possible candidate for the endpoints of a trace link that connects
 * corresponding elements of an architecture model and a code model. An endpoint
 * tuple cannot consist of two architecture endpoints or of two code endpoints.
 */
public class EndpointTuple {

    private final ArchitectureItem architectureEndpoint;
    private final CodeCompilationUnit codeEndpoint;

    /**
     * Created a new endpoint tuple with the specified architecture endpoint and the
     * specified code endpoint.
     *
     * @param architectureEndpoint the architecture endpoint of the endpoint tuple
     *                             to be created
     * @param codeEndpoint         the code endpoint of the endpoint tuple to be
     *                             created
     */
    public EndpointTuple(ArchitectureItem architectureEndpoint, CodeCompilationUnit codeEndpoint) {
        this.architectureEndpoint = architectureEndpoint;
        this.codeEndpoint = codeEndpoint;
    }

    /**
     * Returns the architecture endpoint of this endpoint tuple.
     *
     * @return the architecture endpoint of this endpoint tuple
     */
    public ArchitectureItem getArchitectureEndpoint() {
        return architectureEndpoint;
    }

    /**
     * Returns the code endpoint of this endpoint tuple.
     *
     * @return the code endpoint of this endpoint tuple
     */
    public CodeCompilationUnit getCodeEndpoint() {
        return codeEndpoint;
    }

    public Entity getOtherEndpoint(Entity endpoint) {
        if (architectureEndpoint.equals(endpoint)) {
            return codeEndpoint;
        }
        if (codeEndpoint.equals(endpoint)) {
            return architectureEndpoint;
        }
        throw new IllegalArgumentException("Endpoint tuple must contain given endpoint");
    }

    public boolean hasEndpoint(Entity endpoint) {
        return architectureEndpoint.equals(endpoint) || codeEndpoint.equals(endpoint);
    }

    public boolean hasEndpoint(EndpointTuple endpointTuple) {
        return architectureEndpoint.equals(endpointTuple.architectureEndpoint) || codeEndpoint.equals(endpointTuple.codeEndpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(architectureEndpoint, codeEndpoint);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EndpointTuple other = (EndpointTuple) obj;
        return Objects.equals(architectureEndpoint, other.architectureEndpoint) && Objects.equals(codeEndpoint, other.codeEndpoint);
    }

    @Override
    public String toString() {
        return "Architecture Endpoint: " + architectureEndpoint + ", Code Endpoint: " + codeEndpoint;
    }
}
