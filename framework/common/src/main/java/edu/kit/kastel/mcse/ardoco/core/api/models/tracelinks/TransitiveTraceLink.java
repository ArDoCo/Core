/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.util.Objects;

public class TransitiveTraceLink extends TraceLink {

    private final TraceLink firstTraceLink;
    private final TraceLink secondTraceLink;

    private TransitiveTraceLink(TraceLink firstTraceLink, TraceLink secondTraceLink) {
        super(new EndpointTuple(firstTraceLink.getEndpointTuple().firstEndpoint(), secondTraceLink.getEndpointTuple().secondEndpoint()));
        this.firstTraceLink = firstTraceLink;
        this.secondTraceLink = secondTraceLink;
    }

    public static TransitiveTraceLink createTransitiveTraceLink(TraceLink firstTraceLink, TraceLink secondTraceLink) {
        if (isValidTransitiveTraceLink(firstTraceLink, secondTraceLink)) {
            return new TransitiveTraceLink(firstTraceLink, secondTraceLink);
        }
        return null; // TODO what to do here?
    }

    public static boolean isValidTransitiveTraceLink(TraceLink firstTraceLink, TraceLink secondTraceLink) {
        var secondEndpointOfFirstTl = firstTraceLink.getEndpointTuple().secondEndpoint();
        var firstEndpointOfSecondTl = secondTraceLink.getEndpointTuple().firstEndpoint();

        return secondEndpointOfFirstTl.equals(firstEndpointOfSecondTl);
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
