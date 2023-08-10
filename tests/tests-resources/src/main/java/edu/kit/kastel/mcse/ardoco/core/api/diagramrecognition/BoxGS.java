package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaGSTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Sentence;

/**
 * Connector for the {@link Box} JSON representation.
 */
public class BoxGS extends Box implements Serializable {
    private final DiagramGS diagramGS;
    private BoundingBox boundingBox;
    private TextBox[] textBoxes;
    private BoxGS[] subBoxes;
    private TraceLinkGS[] tracelinks;

    /**
     * Create a new box from the goldstandard.
     *
     * @param boundingBox the {@link BoundingBox} of the box.
     * @param textBoxes   all {@link TextBox} instances that are directly contained in this box (does not include sub boxes!)
     * @param subBoxes    all subboxes that are contained withing the bounding box of this box
     * @param tracelinks  all tracelinks associated with this box (does not include sub boxes!)
     */
    @JsonCreator
    public BoxGS(@JacksonInject DiagramGS diagram, @JsonProperty("boundingBox") BoundingBox boundingBox, @JsonProperty("textBoxes") TextBox[] textBoxes,
            @JsonProperty("subBoxes") BoxGS[] subBoxes, @JsonProperty("tracelinks") TraceLinkGS[] tracelinks) {
        super(diagram, getUUID(boundingBox), boundingBox.toCoordinates(), 1, Classification.UNKNOWN.getClassificationString(), Arrays.asList(textBoxes), null);
        this.diagramGS = diagram;
        this.boundingBox = boundingBox;
        this.textBoxes = textBoxes;
        this.subBoxes = subBoxes;
        this.tracelinks = tracelinks;
    }

    private static String getUUID(BoundingBox boundingBox) {
        return Arrays.stream(boundingBox.toCoordinates()).mapToObj(Integer::toString).reduce("box", (l, r) -> l + "-" + r);
    }

    public ImmutableSet<DiaGSTraceLink> getTraceLinks(List<Sentence> sentences) {
        var list = Arrays.stream(tracelinks)
                .filter(t -> getDiagram().getDiagramProject().getTextResourceName().contains(t.name()))
                .flatMap(t -> t.toTraceLinks(this, sentences).stream())
                .toList();
        var set = Sets.immutable.ofAll(list);
        assert set.size() == list.size();
        return set;
    }

    public BoxGS[] getSubBoxes() {
        return subBoxes;
    }

    public @NotNull DiagramGS getDiagram() {
        return this.diagramGS;
    }

    @Override
    public String toString() {
        var allText = getTexts().stream().map(TextBox::getText).reduce((l, r) -> l + " | " + r).orElse("");
        var preText = getDiagram() + "/";
        return preText + allText.substring(0, Math.min(allText.length(), 20));
    }
}
