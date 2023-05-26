/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.codetraceability;

import java.util.Collection;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface SadSamCodeTraceabilityState extends PipelineStepData {
    String ID = "SadSamCodeTraceabilityState";

    /**
     * Add a {@link SamCodeTraceLink} to this state.
     *
     * @param traceLink the trace link to add
     * @return whether the operation was successful
     */
    boolean addTraceLink(TransitiveTraceLink traceLink);

    /**
     * Add a collection of {@link SamCodeTraceLink SamCodeTraceLinks} to this state.
     *
     * @param traceLinks the trace links to add
     * @return whether the operation was successful
     */
    boolean addTraceLinks(Collection<TransitiveTraceLink> traceLinks);
}
