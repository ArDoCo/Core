/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.util.Objects;

public class TransitiveTraceLink extends TraceLink {

    private final TraceLink firstTraceLink;
    private final TraceLink secondTraceLink;

    public TransitiveTraceLink(TraceLink firstTraceLink, TraceLink secondTraceLink) {
        super(new EndpointTuple(firstTraceLink.getEndpointTuple().firstEndpoint(), secondTraceLink.getEndpointTuple().secondEndpoint()));
        this.firstTraceLink = firstTraceLink;
        this.secondTraceLink = secondTraceLink;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEndpointTuple());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TransitiveTraceLink other)) {
            return false;
        }
        return Objects.equals(getEndpointTuple(), other.getEndpointTuple());
    }

    @Override
    public String toString() {
        return getEndpointTuple().toString();
    }
}
