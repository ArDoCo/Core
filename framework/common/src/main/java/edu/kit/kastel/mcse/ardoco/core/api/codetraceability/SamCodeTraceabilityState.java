/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.codetraceability;

import java.util.Collection;

import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface SamCodeTraceabilityState extends PipelineStepData {
    String ID = "SamCodeTraceabilityState";

    /**
     * Add a {@link SamCodeTraceLink} to this state.
     *
     * @param traceLink the trace link to add
     * @return whether the operation was successful
     */
    boolean addTraceLink(SamCodeTraceLink traceLink);

    /**
     * Add a collection of {@link SamCodeTraceLink SamCodeTraceLinks} to this state.
     *
     * @param traceLinks the trace links to add
     * @return whether the operation was successful
     */
    boolean addTraceLinks(Collection<SamCodeTraceLink> traceLinks);

    ImmutableSet<SamCodeTraceLink> getTraceLinks();
}
