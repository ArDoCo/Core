package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;
import java.util.Arrays;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaGSTraceLink;

public record TracelinkG(String name, int[] sentences) implements Serializable {
    public ImmutableSet<DiaGSTraceLink> toTraceLinks(BoxG boxG) {
        var project = boxG.getDiagram().getProject();
        var list = Arrays.stream(sentences).mapToObj(i -> new DiaGSTraceLink(boxG, project.getSentences().get(i - 1), project.name(), name)).toList();
        var set = Sets.immutable.ofAll(list);
        assert set.size() == list.size(); //Otherwise there are duplicates in the goldstandard
        return set;
    }
}
