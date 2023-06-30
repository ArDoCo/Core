package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Connector for the {@link TextBox} JSON representation.
 */
public class TextBoxG extends TextBox {
    public final String text;
    public final BoundingBoxG boundingBox;

    @JsonCreator
    public TextBoxG(@JsonProperty("text") String text, @JsonProperty("boundingBox") BoundingBoxG boundingBox) {
        super(boundingBox.x, boundingBox.y, boundingBox.w, boundingBox.h, 1, text, null);
        this.text = text;
        this.boundingBox = boundingBox;
    }
}
