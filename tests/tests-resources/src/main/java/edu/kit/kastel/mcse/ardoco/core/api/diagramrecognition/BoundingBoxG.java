package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

/**
 * Connector for the {@link Box} and {@link TextBox} coordinates JSON representation.
 */
public class BoundingBoxG {
    public int x;
    public int y;
    public int w;
    public int h;

    public int[] toCoordinates() {
        return new int[] { x, y, x + w, y + h };
    }
}
