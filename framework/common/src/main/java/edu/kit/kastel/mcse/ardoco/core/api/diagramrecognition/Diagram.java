/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityComparable;
import java.io.File;
import java.io.Serializable;
import java.util.List;

public interface Diagram extends SimilarityComparable, Serializable {
    String getResourceName();

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

    @Override
    default boolean similar(Object obj) {
        if (equals(obj)) return true;
        if (obj instanceof Diagram other) {
            return SimilarityComparable.similar(getBoxes(), other.getBoxes());
        }
        return false;
    }
}
