package edu.kit.kastel.mcse.ardoco.core.codetraceability;

import java.util.Collection;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.SamCodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;

public class SamCodeTraceabilityStateImpl extends AbstractState implements SamCodeTraceabilityState {

    private transient MutableList<SamCodeTraceLink> traceLinks = Lists.mutable.empty();

    @Override
    public boolean addTraceLink(SamCodeTraceLink traceLink) {
        return traceLinks.add(traceLink);
    }

    @Override
    public boolean addTraceLinks(Collection<SamCodeTraceLink> traceLinks) {
        return this.traceLinks.addAll(traceLinks);
    }

}
