/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition;

import static java.lang.Math.abs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private transient List<TextBox> textBoxes = new ArrayList<>();
    private transient Integer dominatingColor = null;

    private Box() {
        // Jackson JSON
    }

    public Box(String uuid, int[] coordinates, double confidence, String classification, List<TextBox> textBoxes, Integer dominatingColor) {
        this.uuid = uuid;
        this.coordinates = coordinates;
        this.confidence = confidence;
        this.classification = classification;
        this.textBoxes = textBoxes;
        this.dominatingColor = dominatingColor;
    }

    public int area() {
        return abs(coordinates[0] - coordinates[2]) * abs(coordinates[1] - coordinates[3]);
    }

    public String getUuid() {
        return uuid;
    }

    public int[] getBox() {
        return Arrays.copyOf(coordinates, coordinates.length);
    }

    public double getConfidence() {
        return confidence;
    }

    public String getClassification() {
        return classification;
    }

    public void addTextBox(TextBox textBox) {
        this.textBoxes.add(textBox);
    }

    public List<TextBox> getTexts() {
        return new ArrayList<>(textBoxes);
    }

    public Integer getDominatingColor() {
        return dominatingColor;
    }

    public void setDominatingColor(Integer dominatingColor) {
        this.dominatingColor = dominatingColor;
    }
}
