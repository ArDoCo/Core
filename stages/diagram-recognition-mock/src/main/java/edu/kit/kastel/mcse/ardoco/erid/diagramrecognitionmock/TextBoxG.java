package edu.kit.kastel.mcse.ardoco.erid.diagramrecognitionmock;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox;

/**
 * Connector for the {@link TextBox} JSON representation.
 */
public class TextBoxG {
    public String text;
    public BoundingBoxG boundingBox;

    public TextBox toTextBox() {
        return new TextBox(boundingBox.x, boundingBox.y, boundingBox.w, boundingBox.h, 1, text, null);
    }
}