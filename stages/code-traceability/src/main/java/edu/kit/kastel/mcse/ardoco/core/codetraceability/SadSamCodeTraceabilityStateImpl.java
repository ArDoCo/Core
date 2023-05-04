/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability;

import java.util.Collection;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.SadSamCodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TransitiveTraceLink;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;

public class SadSamCodeTraceabilityStateImpl extends AbstractState implements SadSamCodeTraceabilityState {

    private transient MutableList<TransitiveTraceLink> traceLinks = Lists.mutable.empty();

    @Override
    public boolean addTraceLink(TransitiveTraceLink traceLink) {
        return traceLinks.add(traceLink);
    }

    @Override
    public boolean addTraceLinks(Collection<TransitiveTraceLink> traceLinks) {
        return this.traceLinks.addAll(traceLinks);
    }

}
