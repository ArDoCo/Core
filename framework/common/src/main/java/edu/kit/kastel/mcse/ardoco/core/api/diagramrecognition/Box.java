/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import static java.lang.Math.abs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This class represents a box that is detected by the image recognition.
 */
public final class Box implements Serializable {
    @JsonProperty
    private String uuid = UUID.randomUUID().toString();
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
    private transient Integer dominatingColor = null;

    private Box() {
        // Jackson JSON
    }

    /**
     * Create a new box that is detected on the image.
     *
     * @param uuid            a unique identifier
     * @param coordinates     the coordinates of two corners of the box in pixel. (x1,y1,x2,y2)
     * @param confidence      a confidence value
     * @param classification  the classification (e.g., "LABEL"), see {@link Classification} for further details
     * @param textBoxes       the text boxes that are attached to this box
     * @param dominatingColor a dominating color in the box (iff present)
     */
    public Box(String uuid, int[] coordinates, double confidence, String classification, List<TextBox> textBoxes, Integer dominatingColor) {
        this.uuid = uuid;
        this.coordinates = coordinates;
        this.confidence = confidence;
        this.classification = classification;
        this.textBoxes = textBoxes;
        this.dominatingColor = dominatingColor;
    }

    /**
     * Calculate the area of the box in square pixel.
     *
     * @return the area of the box
     */
    public int area() {
        return abs(coordinates[0] - coordinates[2]) * abs(coordinates[1] - coordinates[3]);
    }

    /**
     * Get the identifier of this box
     *
     * @return a UUID of the box
     */
    public String getUUID() {
        return uuid;
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

    /**
     * Get the dominating color of the box (iff present)
     *
     * @return the dominating color or {@code null} if not present
     */
    public Integer getDominatingColor() {
        return dominatingColor;
    }

    /**
     * Set the dominating color of the box as RGB value.
     *
     * @param dominatingColor the dominating color
     */
    public void setDominatingColor(Integer dominatingColor) {
        this.dominatingColor = dominatingColor;
    }
}
