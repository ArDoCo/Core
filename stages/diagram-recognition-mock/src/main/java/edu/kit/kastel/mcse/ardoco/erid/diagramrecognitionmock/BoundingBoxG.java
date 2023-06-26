package edu.kit.kastel.mcse.ardoco.erid.diagramrecognitionmock;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox;

/**
 * Connector for the {@link Box} and {@link TextBox} coordinates JSON representation.
 */
public class BoundingBoxG {
    @SuppressWarnings("checkstyle:MemberName")
    public int x;
    @SuppressWarnings("checkstyle:MemberName")
    public int y;
    @SuppressWarnings("checkstyle:MemberName")
    public int w;
    @SuppressWarnings("checkstyle:MemberName")
    public int h;

    public int[] toCoordinates() {
        return new int[] { x, y, x + w, y + h };
    }
}
