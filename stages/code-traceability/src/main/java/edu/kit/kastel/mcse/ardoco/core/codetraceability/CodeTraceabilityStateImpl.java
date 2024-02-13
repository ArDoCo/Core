/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.factory.Sets;

import edu.kit.kastel.mcse.ardoco.core.api.codetraceability.CodeTraceabilityState;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SadCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.SamCodeTraceLink;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;
import edu.kit.kastel.mcse.ardoco.core.data.AbstractState;

@Deterministic
public class CodeTraceabilityStateImpl extends AbstractState implements CodeTraceabilityState {

    private MutableList<SamCodeTraceLink> samCodeTraceLinks = Lists.mutable.empty();
    private MutableList<SadCodeTraceLink> transitiveTraceLinks = Lists.mutable.empty();

    public CodeTraceabilityStateImpl() {
        super();
    }

    @Override
    public boolean addSamCodeTraceLink(SamCodeTraceLink traceLink) {
        return this.samCodeTraceLinks.add(traceLink);
    }

    @Override
    public boolean addSamCodeTraceLinks(Collection<SamCodeTraceLink> traceLinks) {
        return this.samCodeTraceLinks.addAll(traceLinks);
    }

    @Override
    public ImmutableSet<SamCodeTraceLink> getSamCodeTraceLinks() {
        return Sets.immutable.withAll(new LinkedHashSet<>(this.samCodeTraceLinks));
    }

    @Override
    public boolean addSadCodeTraceLink(SadCodeTraceLink traceLink) {
        return this.transitiveTraceLinks.add(traceLink);
    }

    @Override
    public boolean addSadCodeTraceLinks(Collection<SadCodeTraceLink> traceLinks) {
        return this.transitiveTraceLinks.addAll(traceLinks);
    }

    @Override
    public ImmutableSet<SadCodeTraceLink> getSadCodeTraceLinks() {
        return this.transitiveTraceLinks.toImmutableSet();
    }

}
