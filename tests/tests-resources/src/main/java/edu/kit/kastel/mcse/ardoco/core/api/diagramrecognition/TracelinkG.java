package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;
import java.util.Arrays;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaTexTraceLink;

public record TracelinkG(String name, int[] sentences) implements Serializable {
    public ImmutableSet<DiaTexTraceLink> toTraceLinks(DiagramElement diagramElement) {
        return Sets.immutable.fromStream(Arrays.stream(sentences).mapToObj(i -> new DiaTexTraceLink(diagramElement, i, name)));
    }
}
