package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeCompilationUnit;

/**
 * A trace link between exactly one architecture endpoint and exactly one code
 * endpoint. Trace links are created to connect corresponding elements of an
 * architecture and a code model.
 */
public class SamCodeTraceLink {

    private EndpointTuple endpointTuple;

    /**
     * Creates a new trace link between an architecture endpoint and a code endpoint
     * as given in the specified endpoint tuple.
     *
     * @param endpointTuple the architecture endpoint and the code endpoint of the
     *                      trace link to be created
     */
    public SamCodeTraceLink(EndpointTuple endpointTuple) {
        this.endpointTuple = endpointTuple;
    }

    public SamCodeTraceLink(ArchitectureItem architectureEndpoint, CodeCompilationUnit codeEndpoint) {
        this.endpointTuple = new EndpointTuple(architectureEndpoint, codeEndpoint);
    }

    /**
     * Returns the endpoint tuple of this trace link.
     *
     * @return the endpoint tuple of this trace link
     */
    public EndpointTuple getEndpointTuple() {
        return endpointTuple;
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpointTuple);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        SamCodeTraceLink other = (SamCodeTraceLink) obj;
        return Objects.equals(endpointTuple, other.endpointTuple);
    }

    @Override
    public String toString() {
        return endpointTuple.toString();
    }
}
