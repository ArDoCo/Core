package edu.kit.kastel.mcse.ardoco.erid.diagramrecognitionmock;

import java.util.Arrays;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Classification;

/**
 * Connector for the {@link Box} JSON representation.
 */
public class BoxG {
    public BoundingBoxG boundingBox;
    public TextBoxG[] textBoxes;
    public BoxG[] subBoxes;
    public TracelinkG[] tracelinks;

    public Box toShallowBox() {
        String uuid = Arrays.stream(boundingBox.toCoordinates()).mapToObj(Integer::toString).reduce("box", (l, r) -> l + "-" + r);
        return new Box(uuid, boundingBox.toCoordinates(), 1, Classification.UNKNOWN.getClassificationString(),
                Arrays.stream(textBoxes).map(TextBoxG::toTextBox).collect(Collectors.toList()), null);
    }
}
