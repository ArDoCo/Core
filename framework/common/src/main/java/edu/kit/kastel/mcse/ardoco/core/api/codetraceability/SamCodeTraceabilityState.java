package edu.kit.kastel.mcse.ardoco.core.api.codetraceability;

import java.util.Collection;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.configuration.IConfigurable;

public interface SamCodeTraceabilityState extends IConfigurable {

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
}
