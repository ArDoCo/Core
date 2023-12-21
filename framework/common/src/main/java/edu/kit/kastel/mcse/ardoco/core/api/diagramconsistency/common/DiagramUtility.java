/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Connector;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * This class contains utility methods to use with the diagram interface.
 */
@Deterministic
public class DiagramUtility {
    private DiagramUtility() {
    }

    /**
     * Checks if there is a connection between the two boxes.
     * 
     * @param diagram The diagram in which the boxes are located.
     * @param source  The source box.
     * @param target  The target box.
     * @return True if there is a connection between the two boxes, false otherwise.
     */
    public static boolean hasConnectionBetween(Diagram diagram, Box source, Box target) {
        return diagram.getConnectors().stream().anyMatch(connector -> isConnectionBetween(connector, source, target));
    }

    /**
     * Checks if the connector connects the two boxes.
     * 
     * @param connector The connector to check.
     * @param source    The source box.
     * @param target    The target box.
     * @return True if the connector connects the two boxes, false otherwise.
     */
    public static boolean isConnectionBetween(Connector connector, Box source, Box target) {
        List<String> connectedBoxes = connector.getConnectedBoxes();
        return connectedBoxes.get(0).equals(source.getUUID()) && connectedBoxes.contains(target.getUUID());
    }

    /**
     * Returns all connectors that are outgoing from the box.
     * 
     * @param diagram The diagram in which the box is located.
     * @param box     The box.
     * @return All connectors that are outgoing from the box.
     */
    public static List<Connector> getOutgoingConnectors(Diagram diagram, Box box) {
        return diagram.getConnectors().stream().filter(connector -> connector.getConnectedBoxes().get(0).equals(box.getUUID())).toList();
    }

    /**
     * Get a map of all boxes in the diagram.
     * 
     * @param diagram The diagram.
     * @return A map from the UUID of the box to the box.
     */
    public static SortedMap<String, Box> getBoxes(Diagram diagram) {
        return diagram.getBoxes().stream().collect(Collectors.toMap(Box::getUUID, box -> box, (a, b) -> b, TreeMap::new));
    }

    /**
     * Get the targets of the connector.
     *
     * @param connector The connector.
     * @param boxes     A UUID-box map.
     * @return The targets of the connector.
     */
    public static List<Box> getTargets(Connector connector, SortedMap<String, Box> boxes) {
        return connector.getConnectedBoxes().stream().skip(1).map(boxes::get).toList();
    }

    /**
     * Get the text of the box.
     * 
     * @param box The box.
     * @return The text of the box.
     */
    public static String getBoxText(Box box) {
        return box.getTexts().stream().map(TextBox::getText).collect(Collectors.joining(" "));
    }

    /**
     * Get the contained boxes of the box.
     * 
     * @param box   The box.
     * @param boxes A UUID-box map.
     * @return The contained boxes of the box.
     */
    public static List<Box> getContainedBoxes(Box box, SortedMap<String, Box> boxes) {
        return box.getContainedBoxes().stream().map(boxes::get).toList();
    }

    /**
     * Add a box to the diagram.
     * 
     * @param diagram The diagram.
     * @param text    The text of the box.
     * @return The added box.
     */
    public static Box addBox(Diagram diagram, String text) {
        TextBox textBox = new TextBox(0, 0, 0, 0, 1.0, text, null);
        Box box = new Box(String.valueOf(diagram.getBoxes().size()), new int[] { 0, 0, 0, 0 }, 1.0, null, List.of(textBox), null);

        diagram.addBox(box);
        return box;
    }

    /**
     * Add a connector between the two boxes.
     * 
     * @param diagram The diagram in which the boxes are located.
     * @param source  The source box.
     * @param target  The target box.
     */
    public static void addConnector(Diagram diagram, Box source, Box target) {
        diagram.addConnector(new Connector(UUID.randomUUID().toString(), List.of(source.getUUID(), target.getUUID()), List.of()));
    }
}
