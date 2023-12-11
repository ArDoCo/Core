/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityComparable;
import edu.kit.kastel.mcse.ardoco.core.data.MetaData;

/**
 * Programmatically represents an informal diagram. A diagram is uniquely identified by its (resource) name and can contain an arbitrary number of diagram
 * elements and connectors between them.
 */
public interface Diagram extends Comparable<Diagram>, SimilarityComparable<Diagram>, Serializable {
    /**
     * {@return the full (resource) name of the diagram, e.g. "some/path/to/some-diagram.jpg"}
     */
    String getResourceName();

    /**
     * {@return the short (resource) name of the diagram, e.g. "some-diagram.jpg"}
     */
    default String getShortResourceName() {
        var split = getResourceName().split("/|\\\\");
        return split[split.length - 1];
    }

    File getLocation();

    void addBox(Box box);

    boolean removeBox(Box box);

    List<Box> getBoxes();

    void addTextBox(TextBox textBox);

    boolean removeTextBox(TextBox textBox);

    /**
     * Returns the raw text boxes that are attached to this diagram. This method does not return the text boxes that are attached to the boxes of this diagram.
     *
     * @return the raw text boxes that are attached to this diagram
     */
    List<TextBox> getTextBoxes();

    void addConnector(Connector connector);

    boolean removeConnector(Connector connector);

    List<Connector> getConnectors();

    @Override
    default boolean similar(MetaData metaData, Diagram obj) {
        if (equals(obj))
            return true;
        if (getResourceName().equals(obj.getResourceName())) {
            return SimilarityComparable.similar(metaData, getBoxes(), obj.getBoxes());
        }
        return false;
    }

    @Override
    default int compareTo(Diagram o) {
        if (equals(o))
            return 0;
        return getResourceName().compareTo(o.getResourceName());
    }
}
