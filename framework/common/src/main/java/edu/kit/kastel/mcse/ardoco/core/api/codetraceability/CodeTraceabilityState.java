/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.codetraceability;

import java.util.Collection;

import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

public interface CodeTraceabilityState extends PipelineStepData {
    String ID = "CodeTraceabilityState";

    /**
     * Add a {@link SamCodeTraceLink} to this state.
     *
     * @param traceLink the trace link to add
     * @return whether the operation was successful
     */
    boolean addSamCodeTraceLink(SamCodeTraceLink traceLink);

    /**
     * Add a collection of {@link SamCodeTraceLink SamCodeTraceLinks} to this state.
     *
     * @param traceLinks the trace links to add
     * @return whether the operation was successful
     */
    boolean addSamCodeTraceLinks(Collection<SamCodeTraceLink> traceLinks);

    /**
     * Return a set of stored {@link SamCodeTraceLink SamCodeTraceLinks}.
     *
     * @return set of stored {@link SamCodeTraceLink SamCodeTraceLinks}
     */
    ImmutableSet<SamCodeTraceLink> getSamCodeTraceLinks();

    /**
     * Add a {@link TransitiveTraceLink} to this state.
     *
     * @param traceLink the trace link to add
     * @return whether the operation was successful
     */
    boolean addSadCodeTraceLink(SadCodeTraceLink traceLink);

    /**
     * Add a collection of {@link SadCodeTraceLink SadCodeTraceLinks} to this state.
     *
     * @param traceLinks the trace links to add
     * @return whether the operation was successful
     */
    boolean addSadCodeTraceLinks(Collection<SadCodeTraceLink> traceLinks);

    /**
     * Return a set of stored {@link TransitiveTraceLink TransitiveTraceLinks}.
     *
     * @return set of stored {@link TransitiveTraceLink TransitiveTraceLinks}
     */
    ImmutableSet<SadCodeTraceLink> getSadCodeTraceLinks();
}
