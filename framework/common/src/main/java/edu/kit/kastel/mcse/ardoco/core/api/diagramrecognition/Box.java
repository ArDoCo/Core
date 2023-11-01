/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import static java.lang.Math.abs;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.common.collection.UnmodifiableLinkedHashSet;

/**
 * This class represents a box that is detected by the image recognition.
 */
public class Box extends DiagramElement implements Serializable {
    // the four coordinates x1,y1,x2,y2

    private int[] coordinates;
    private double confidence;
    private String classification;
    private List<TextBox> textBoxes = new ArrayList<>();
    private Color dominatingColor = null;
    @JsonIgnore
    private LinkedHashSet<String> references = new LinkedHashSet<>();

    private static @NotNull String calculateUUID(@NotNull int[] coordinates) {
        return String.format("Box [%s]", getBoundingBoxConcat(coordinates));
    }

    /**
     * {@return the bounding box coordinates joined with hyphens}
     *
     * @param coordinates bounding box coordinates
     */
    public static @NotNull String getBoundingBoxConcat(@NotNull int[] coordinates) {
        return Arrays.stream(coordinates).mapToObj((Integer::toString)).reduce((l, r) -> l + "-" + r).orElseThrow();
    }

    // Jackson JSON
    //FIXME I think this may be removed, not sure if it is really no longer in use though. There is a dedicated JSONCreator constructor, so it should be fine. Empty super constructor may also be removed if that is the case.
    private Box() {
        super();
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
     * @param diagram        the diagram this box belongs to
     * @param coordinates    the coordinates of two corners of the box in pixel. (x1,y1,x2,y2)
     * @param confidence     a confidence value
     * @param classification the classification (e.g., "LABEL"), see {@link Classification} for further details
     */
    @JsonCreator
    public Box(@JacksonInject Diagram diagram, @JsonProperty("box") int[] coordinates, @JsonProperty("confidence") double confidence,
            @JsonProperty("class") String classification) {
        super(diagram, calculateUUID(coordinates));
        this.coordinates = coordinates;
        this.confidence = confidence;
        this.classification = classification;
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
    public @NotNull String getUUID() {
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
    public @NotNull Classification getClassification() {
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
     * Remove a text box that is associated with the box.
     *
     * @param textBox the textbox
     */
    public void removeTextBox(@NotNull TextBox textBox) {
        Objects.requireNonNull(textBox);
        this.textBoxes.removeIf(it -> it == textBox);
    }

    /**
     * Get all text boxes that are associated with the box.
     *
     * @return all associated text boxes
     */
    public @NotNull List<TextBox> getTexts() {
        return new ArrayList<>(textBoxes);
    }

    public @NotNull UnmodifiableLinkedHashSet<String> getReferences() {
        return new UnmodifiableLinkedHashSet<>(references);
    }

    /**
     * Adds a new reference to the set of references.
     *
     * @param reference the reference string
     * @return true if the reference wasn't already contained, false otherwise
     */
    public boolean addReference(@NotNull String reference) {
        return references.add(reference);
    }

    public void setReferences(@NotNull List<String> references) {
        this.references = new LinkedHashSet<>(references);
    }

    /**
     * Tries to remove the given reference from the references
     *
     * @param reference the reference
     * @return true if removed, false otherwise
     */
    public boolean removeReference(@NotNull String reference) {
        return references.remove(reference);
    }

    /**
     * Get the dominating color of the box (iff present)
     *
     * @return the dominating color or {@code null} if not present
     */
    public @NotNull Color getDominatingColor() {
        return dominatingColor;
    }

    /**
     * Set the dominating color of the box as RGB value.
     *
     * @param dominatingColor the dominating color
     */
    public void setDominatingColor(@NotNull Color dominatingColor) {
        this.dominatingColor = dominatingColor;
    }

    @Override
    public @NotNull BoundingBox getBoundingBox() {
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
}
