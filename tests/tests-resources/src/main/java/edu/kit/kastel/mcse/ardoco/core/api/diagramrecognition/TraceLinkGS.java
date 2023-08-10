package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaGSTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;

public record TraceLinkGS(@JsonProperty("name") String name, @JsonProperty("sentences") int[] sentenceIds, @JsonProperty("typedTracelinks") TypedTraceLinkGS[] typedTracelinks) implements Serializable {
    public ImmutableSet<DiaGSTraceLink> toTraceLinks(BoxGS boxGS, List<Sentence> sentences) {
        //From sentences
        var list = toTraceLinks(boxGS, sentences, sentenceIds, TraceType.ENTITY);
        //From typed trace links
        var typedList = List.<DiaGSTraceLink>of();
        if (typedTracelinks != null)
            typedList = Arrays.stream(typedTracelinks).flatMap(typed -> toTraceLinks(boxGS, sentences, typed.sentences(), typed.traceType()).stream()).toList();
        list.addAll(typedList);

        var set = Sets.immutable.ofAll(list);
        assert set.size() == list.size(); //Otherwise there are duplicates in the goldstandard
        return set;
    }

    private ArrayList<DiaGSTraceLink> toTraceLinks(BoxGS boxGS, List<Sentence> sentences, int[] sentenceIds, TraceType traceType) {
        var project = boxGS.getDiagram().getDiagramProject();
        return Arrays.stream(sentenceIds)
                .mapToObj(i -> new DiaGSTraceLink(boxGS, sentences.get(i - 1), project.name(), name, traceType))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
