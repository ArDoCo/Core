/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

import java.util.Objects;
import java.util.Optional;

public class TransitiveTraceLink extends SadCodeTraceLink {

    private final TraceLink firstTraceLink;
    private final TraceLink secondTraceLink;

    private TransitiveTraceLink(TraceLink firstTraceLink, TraceLink secondTraceLink) {
        super(new EndpointTuple(firstTraceLink.getEndpointTuple().firstEndpoint(), secondTraceLink.getEndpointTuple().secondEndpoint()));
        this.firstTraceLink = firstTraceLink;
        this.secondTraceLink = secondTraceLink;
    }

    public static Optional<TransitiveTraceLink> createTransitiveTraceLink(TraceLink firstTraceLink, TraceLink secondTraceLink) {
        if (isValidTransitiveTraceLink(firstTraceLink, secondTraceLink)) {
            return Optional.of(new TransitiveTraceLink(firstTraceLink, secondTraceLink));
        }
        return Optional.empty();
    }

    public static boolean isValidTransitiveTraceLink(TraceLink firstTraceLink, TraceLink secondTraceLink) {
        var secondEndpointOfFirstTl = firstTraceLink.getEndpointTuple().secondEndpoint().getId();
        var firstEndpointOfSecondTl = secondTraceLink.getEndpointTuple().firstEndpoint().getId();

        return secondEndpointOfFirstTl.equals(firstEndpointOfSecondTl);
    }

    public TraceLink getFirstTraceLink() {
        return firstTraceLink;
    }

    public TraceLink getSecondTraceLink() {
        return secondTraceLink;
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
        return Objects.equals(getFirstTraceLink(), other.getFirstTraceLink()) && //
                Objects.equals(getSecondTraceLink(), other.getSecondTraceLink()) && //
                Objects.equals(getEndpointTuple(), other.getEndpointTuple());
    }

    @Override
    public String toString() {
        return getEndpointTuple().toString();
    }
}
