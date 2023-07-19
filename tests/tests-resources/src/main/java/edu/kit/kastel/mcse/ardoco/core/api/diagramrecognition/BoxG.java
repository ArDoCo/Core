package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;
import java.util.Arrays;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaGSTraceLink;

/**
 * Connector for the {@link Box} JSON representation.
 */
public class BoxG extends Box implements Serializable {
    private final DiagramG diagramG;
    private BoundingBoxG boundingBox;
    private TextBoxG[] textBoxes;
    private BoxG[] subBoxes;
    private TracelinkG[] tracelinks;

    /**
     * Create a new box from the goldstandard.
     *
     * @param boundingBox the {@link BoundingBoxG} of the box.
     * @param textBoxes   all {@link TextBoxG} instances that are directly contained in this box (does not include sub boxes!)
     * @param subBoxes    all subboxes that are contained withing the bounding box of this box
     * @param tracelinks  all tracelinks associated with this box (does not include sub boxes!)
     */
    @JsonCreator
    public BoxG(@JacksonInject DiagramG diagram, @JsonProperty("boundingBox") BoundingBoxG boundingBox, @JsonProperty("textBoxes") TextBoxG[] textBoxes,
            @JsonProperty("subBoxes") BoxG[] subBoxes, @JsonProperty("tracelinks") TracelinkG[] tracelinks) {
        super(diagram, getUUID(boundingBox), boundingBox.toCoordinates(), 1, Classification.UNKNOWN.getClassificationString(), Arrays.asList(textBoxes), null);
        this.diagramG = diagram;
        this.boundingBox = boundingBox;
        this.textBoxes = textBoxes;
        this.subBoxes = subBoxes;
        this.tracelinks = tracelinks;
    }

    private static String getUUID(BoundingBoxG boundingBox) {
        return Arrays.stream(boundingBox.toCoordinates()).mapToObj(Integer::toString).reduce("box", (l, r) -> l + "-" + r);
    }

    public ImmutableSet<DiaGSTraceLink> getTraceLinks() {
        var list = Arrays.stream(tracelinks)
                .filter(t -> getDiagram().getProject().getText().contains(t.name()))
                .flatMap(t -> t.toTraceLinks(this).stream())
                .toList();
        var set = Sets.immutable.ofAll(list);
        assert set.size() == list.size();
        return set;
    }

    public BoxG[] getSubBoxes() {
        return subBoxes;
    }

    public @NotNull DiagramG getDiagram() {
        return this.diagramG;
    }

    @Override
    public String toString() {
        var allText = getTexts().stream().map(TextBox::getText).reduce((l, r) -> l + " | " + r).orElse("");
        var preText = getDiagram() + "/";
        return preText + allText.substring(0, Math.min(allText.length(), 20));
    }
}
