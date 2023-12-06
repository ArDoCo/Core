/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramGoldStandardTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;
import edu.kit.kastel.mcse.ardoco.core.common.collection.UnmodifiableLinkedHashSet;

/**
 * Encapsulates the diagram-sentence trace links to its parent diagram element. Used for deserialization purposes.
 *
 * @param name            the name of the text file the sentence numbers refer to
 * @param sentenceIds     contains the sentence number of each trace link
 * @param typedTracelinks a set of trace links with additional information
 */
public record TraceLinkGS(@JsonProperty("name") String name, @JsonProperty("sentences") int[] sentenceIds,
                          @JsonProperty("typedTracelinks") TypedTraceLinkGS[] typedTracelinks) implements Serializable {
    /**
     * {@return the set of diagram-sentence trace links associated with this box} The sentences are used to resolve the sentence numbers to actual sentences
     * from the document.
     *
     * @param boxGS     the box this trace link points to
     * @param sentences the sentences from the text
     */
    public UnmodifiableLinkedHashSet<DiagramGoldStandardTraceLink> toTraceLinks(BoxGS boxGS, List<Sentence> sentences) {
        //From sentences, set default trace type ENTITY
        var list = toTraceLinks(boxGS, sentences, sentenceIds, TraceType.ENTITY);
        //From typed trace links
        var typedList = List.<DiagramGoldStandardTraceLink>of();
        if (typedTracelinks != null)
            typedList = Arrays.stream(typedTracelinks).flatMap(typed -> toTraceLinks(boxGS, sentences, typed.sentences(), typed.traceType()).stream()).toList();
        list.addAll(typedList);

        var set = UnmodifiableLinkedHashSet.of(list);
        assert set.size() == list.size(); //Otherwise there are duplicates in the goldstandard
        return set;
    }

    private ArrayList<DiagramGoldStandardTraceLink> toTraceLinks(BoxGS boxGS, List<Sentence> sentences, int[] sentenceIds, TraceType traceType) {
        var project = boxGS.getDiagram().getDiagramProject();
        return Arrays.stream(sentenceIds)
                .mapToObj(i -> new DiagramGoldStandardTraceLink(boxGS, sentences.get(i - 1), project.name(), name, traceType))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}