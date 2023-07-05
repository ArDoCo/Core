package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import com.fasterxml.jackson.annotation.JacksonInject;

import java.util.Arrays;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaTexTraceLink;

/**
 * Connector for the {@link Box} JSON representation.
 */
public class BoxG extends Box {
    private final BoundingBoxG boundingBox;
    private final TextBoxG[] textBoxes;
    private final BoxG[] subBoxes;
    private final TracelinkG[] tracelinks;

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
        this.boundingBox = boundingBox;
        this.textBoxes = textBoxes;
        this.subBoxes = subBoxes;
        this.tracelinks = tracelinks;
    }

    private static String getUUID(BoundingBoxG boundingBox) {
        return Arrays.stream(boundingBox.toCoordinates()).mapToObj(Integer::toString).reduce("box", (l, r) -> l + "-" + r);
    }

    public ImmutableSet<DiaTexTraceLink> getTraceLinks() {
        return Sets.immutable.fromStream(Arrays.stream(tracelinks).flatMap(t -> t.toTraceLinks(this).stream()));
    }

    public BoxG[] getSubBoxes() {
        return subBoxes;
    }

    @Override
    public String toString() {
        var allText = getTexts().stream().map(TextBox::getText).reduce((l, r) -> l + " | " + r).orElse("");
        var preText = getDiagram() + "/";
        return preText + allText.substring(0, Math.min(allText.length(), 20));
    }
}
