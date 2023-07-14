package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;

/**
 * Connector for the {@link Box} and {@link TextBox} coordinates JSON representation.
 */
public class BoundingBoxG implements Serializable {
    public int x;
    public int y;
    public int w;
    public int h;

    public int[] toCoordinates() {
        return new int[] { x, y, x + w, y + h };
    }
}
