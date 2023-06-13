package edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks;

public class SadCodeTraceLink extends TraceLink {

    public SadCodeTraceLink(EndpointTuple endpointTuple) {
        super(endpointTuple);
    }

    @Override
    public String toString() {
        return getEndpointTuple().toString();
    }
}
