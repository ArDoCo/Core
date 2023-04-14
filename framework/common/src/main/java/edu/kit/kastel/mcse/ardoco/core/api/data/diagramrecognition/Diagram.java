/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition;

import java.util.List;

public interface Diagram {
    String getLocation();

    List<Box> getBoxes();

    List<TextBox> getTextBoxes();

    List<Connector> getConnectors();
}
