/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import static java.lang.Math.abs;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a box that is detected by the image recognition.
 */
public class Box extends DiagramElement implements Serializable {
    // the four coordinates x1,y1,x2,y2
    @JsonProperty("box")
    private int[] coordinates;
    @JsonProperty
    private double confidence;
    @JsonProperty("class")
    private String classification;
    @JsonProperty("texts")
    private List<TextBox> textBoxes = new ArrayList<>();
    @JsonProperty("contained")
    private List<String> containedBoxes = new ArrayList<>();
    @JsonProperty("dominatingColor")
    private Color dominatingColor = null;
    @JsonIgnore
    private SortedSet<String> references = new TreeSet<>();

    protected static String calculateUUID(int[] coordinates) {
        return String.format("Box [%s]", getBoundingBoxConcat(coordinates));
    }

    /**
     * {@return the bounding box coordinates joined with hyphens}
     *
     * @param coordinates bounding box coordinates
     */
    public static String getBoundingBoxConcat(int[] coordinates) {
        return Arrays.stream(coordinates).mapToObj((Integer::toString)).reduce((l, r) -> l + "-" + r).orElseThrow();
    }

    /**
     * Create a new box that is detected on the image.
     *
     * @param diagram         the diagram the box belongs to
     * @param uuid            the unique identifier of the box
     * @param coordinates     the coordinates of two corners of the box in pixel. (x1,y1,x2,y2)
     * @param confidence      a confidence value
     * @param classification  the classification (e.g., "LABEL"), see {@link Classification} for further details
     * @param textBoxes       the text boxes that are attached to this box
     * @param dominatingColor a dominating color in the box (iff present)
     */
    public Box(Diagram diagram, String uuid, int[] coordinates, double confidence, String classification, List<TextBox> textBoxes, Color dominatingColor) {
        super(diagram, uuid);
        this.coordinates = coordinates;
        this.confidence = confidence;
        this.classification = classification;
        this.textBoxes = textBoxes;
        this.dominatingColor = dominatingColor;
    }

    /**
     * Create a new box that is detected on the image.
     *
     * @param diagram         the diagram the box belongs to
     * @param coordinates     the coordinates of two corners of the box in pixel. (x1,y1,x2,y2)
     * @param confidence      a confidence value
     * @param classification  the classification (e.g., "LABEL"), see {@link Classification} for further details
     * @param textBoxes       the text boxes that are attached to this box
     * @param dominatingColor a dominating color in the box (iff present)
     */
    public Box(Diagram diagram, int[] coordinates, double confidence, String classification, List<TextBox> textBoxes, Color dominatingColor) {
        super(diagram, calculateUUID(coordinates));
        this.coordinates = coordinates;
        this.confidence = confidence;
        this.classification = classification;
        this.textBoxes = textBoxes;
        this.dominatingColor = dominatingColor;
    }

    /**
     * Create a new box that is detected on the image.
     *
     * @param diagram         the diagram this box belongs to
     * @param uuid            the unique identifier of the box
     * @param coordinates     the coordinates of two corners of the box in pixel. (x1,y1,x2,y2)
     * @param confidence      a confidence value
     * @param classification  the classification (e.g., "LABEL"), see {@link Classification} for further details
     * @param dominatingColor a dominating color in the box (iff present)
     */
    @JsonCreator
    public Box(@JacksonInject Diagram diagram, @JsonProperty("uuid") String uuid, @JsonProperty("box") int[] coordinates,
            @JsonProperty("confidence") double confidence, @JsonProperty("class") String classification,
            @JsonProperty("dominatingColor") Color dominatingColor) {
        super(diagram, uuid == null ? calculateUUID(coordinates) : uuid);
        this.coordinates = coordinates;
        this.confidence = confidence;
        this.classification = classification;
        this.dominatingColor = dominatingColor;
    }

    /**
     * Calculate the area of the box in square pixel.
     *
     * @return the area of the box
     */
    //TODO Delegate to bounding box class
    public int area() {
        return abs(coordinates[0] - coordinates[2]) * abs(coordinates[1] - coordinates[3]);
    }

    /**
     * Get the identifier of this box
     *
     * @return a UUID of the box
     */
    public String getUUID() {
        // We stored the UUID in name.
        return getName();
    }

    /**
     * Get the coordinates of two corners of the box in pixel. (x1,y1,x2,y2)
     *
     * @return the coordinates
     */
    public int[] getBox() {
        return Arrays.copyOf(coordinates, coordinates.length);
    }

    /**
     * Get the confidence of the recognized box.
     *
     * @return a confidence value
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * Get the classification (e.g., "LABEL").
     *
     * @return the classification
     */
    public Classification getClassification() {
        return Classification.byString(classification);
    }

    /**
     * Add a text box that shall be associated with the box.
     *
     * @param textBox the textbox
     */
    public void addTextBox(TextBox textBox) {
        this.textBoxes.add(Objects.requireNonNull(textBox));
    }

    /**
     * Mark another box as contained in this box.
     *
     * @param box the contained box
     */
    public void addContainedBox(Box box) {
        this.containedBoxes.add(Objects.requireNonNull(box.getUUID()));
    }

    /**
     * Get all boxes that have been marked as contained in this box.
     * More boxes might be contained, to find these the overlapping area of the boxes has to be calculated.
     *
     * @return all contained boxes
     */
    public List<String> getContainedBoxes() {
        return new ArrayList<>(containedBoxes);
    }

    /**
     * Remove a text box that is associated with the box.
     *
     * @param textBox the textbox
     */
    public void removeTextBox(TextBox textBox) {
        Objects.requireNonNull(textBox);
        this.textBoxes.removeIf(it -> it == textBox);
    }

    /**
     * Get all text boxes that are associated with the box.
     *
     * @return all associated text boxes
     */
    public List<TextBox> getTexts() {
        return new ArrayList<>(textBoxes);
    }

    public SortedSet<String> getReferences() {
        return new TreeSet<>(references);
    }

    /**
     * Adds a new reference to the set of references.
     *
     * @param reference the reference string
     * @return true if the reference wasn't already contained, false otherwise
     */
    public boolean addReference(String reference) {
        return references.add(reference);
    }

    public void setReferences(List<String> references) {
        this.references = new TreeSet<>(references);
    }

    /**
     * Tries to remove the given reference from the references
     *
     * @param reference the reference
     * @return true if removed, false otherwise
     */
    public boolean removeReference(String reference) {
        return references.remove(reference);
    }

    /**
     * Get the dominating color of the box (iff present)
     *
     * @return the dominating color or {@code null} if not present
     */
    public Color getDominatingColor() {
        return dominatingColor;
    }

    /**
     * Set the dominating color of the box as RGB value.
     *
     * @param dominatingColor the dominating color
     */
    public void setDominatingColor(Color dominatingColor) {
        this.dominatingColor = dominatingColor;
    }

    @Override
    public BoundingBox getBoundingBox() {
        return new BoundingBox(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Returns a string representation of the box, if wrap is set to true, the class name is appended before string
     *
     * @param wrap whether the class name should be appended
     * @return a string representation of the box
     */
    public String toString(boolean wrap) {
        var formatted = String.format("%s %s", Arrays.stream(coordinates).mapToObj((Integer::toString)).reduce((l, r) -> l + "-" + r).orElseThrow(),
                getReferences().stream().findFirst().orElse("REFERENCES_NOT_SET"));
        if (wrap)
            return String.format("Box [%s]", formatted);
        return formatted;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof Box otherBox && this.textBoxes.equals(otherBox.textBoxes);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
