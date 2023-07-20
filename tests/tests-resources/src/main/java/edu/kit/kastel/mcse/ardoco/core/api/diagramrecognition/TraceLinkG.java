package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaGSTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;

public record TraceLinkG(String name, int[] sentences, TypedTraceLinkG[] typedTracelinks) implements Serializable {
    public ImmutableSet<DiaGSTraceLink> toTraceLinks(BoxG boxG) {
        //From sentences
        var list = toTraceLinks(boxG, sentences, TraceType.ENTITY);
        //From typed trace links
        var typedList = List.<DiaGSTraceLink>of();
        if (typedTracelinks != null)
            typedList = Arrays.stream(typedTracelinks).flatMap(typed -> toTraceLinks(boxG, typed.sentences(), typed.traceType()).stream()).toList();
        list.addAll(typedList);

        var set = Sets.immutable.ofAll(list);
        assert set.size() == list.size(); //Otherwise there are duplicates in the goldstandard
        return set;
    }

    private ArrayList<DiaGSTraceLink> toTraceLinks(BoxG boxG, int[] sentences, TraceType traceType) {
        var project = boxG.getDiagram().getProject();
        return Arrays.stream(sentences)
                .mapToObj(i -> new DiaGSTraceLink(boxG, project.getSentences().get(i - 1), project.name(), name, traceType))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
