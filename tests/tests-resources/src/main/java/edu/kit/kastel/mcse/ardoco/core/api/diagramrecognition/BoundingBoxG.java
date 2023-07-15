package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;

/**
 * Connector for the {@link Box} and {@link TextBox} coordinates JSON representation.
 */
public record BoundingBoxG(int x, int y, int w, int h) implements Serializable {
    public int[] toCoordinates() {
        return new int[] { x, y, x + w, y + h };
    }
}
