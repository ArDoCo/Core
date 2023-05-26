/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.File;
import java.util.List;

public interface Diagram {
    File getLocation();

    void addBox(Box box);

    boolean removeBox(Box box);

    List<Box> getBoxes();

    void addTextBox(TextBox textBox);

    boolean removeTextBox(TextBox textBox);

    List<TextBox> getTextBoxes();

    void addConnector(Connector connector);

    boolean removeConnector(Connector connector);

    List<Connector> getConnectors();
}
