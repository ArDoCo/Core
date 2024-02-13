/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityComparable;
import edu.kit.kastel.mcse.ardoco.core.data.GlobalConfiguration;

/**
 * A text
 */
public class TextBox implements SimilarityComparable<TextBox>, Serializable {
    private static final double SIMILARITY_THRESHOLD = 0.85;
    private final BoundingBox boundingBox;
    private final double confidence;
    private final String text;
    private Color dominatingColor = null;

    public TextBox(BoundingBox boundingBox, double confidence, String text) {
        this.boundingBox = boundingBox;
        this.confidence = confidence;
        this.text = text;
    }

    public TextBox(BoundingBox boundingBox, double confidence, String text, Color dominatingColor) {
        this(boundingBox, confidence, text);
        this.dominatingColor = dominatingColor;
    }

    @JsonCreator
    public TextBox(@JsonProperty("x") int xCoordinate, @JsonProperty("y") int yCoordinate, @JsonProperty("w") int width, @JsonProperty("h") int height,
            @JsonProperty("confidence") double confidence, @JsonProperty("text") String text) {
        this(new BoundingBox(xCoordinate, yCoordinate, xCoordinate + width, yCoordinate + height), confidence, text);
    }

    public TextBox(int x, int y, int w, int h, double confidence, String text, Color dominatingColor) {
        this(x, y, w, h, confidence, text);
        this.setDominatingColor(dominatingColor);
    }

    /**
     * Get the coordinates of the absolute box as (x1,y1,x2,y2) in pixel.
     *
     * @return the absolute coordinates of the box
     */
    public int[] absoluteBox() {
        return new int[] { boundingBox.minX(), boundingBox.minY(), boundingBox.maxX(), boundingBox.maxY() };
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public int area() {
        return getWidth() * getHeight();
    }

    public int getXCoordinate() {
        return boundingBox.minX();
    }

    public int getYCoordinate() {
        return boundingBox.minY();
    }

    public int getWidth() {
        return boundingBox.width();
    }

    public int getHeight() {
        return boundingBox.height();
    }

    public double getConfidence() {
        return confidence;
    }

    public String getText() {
        return text;
    }

    public Color getDominatingColor() {
        return dominatingColor;
    }

    public void setDominatingColor(Color dominatingColor) {
        this.dominatingColor = dominatingColor;
    }

    @Override
    public String toString() {
        return String.format("TextBox [text=%s]", getText());
    }

    @Override
    public boolean similar(GlobalConfiguration globalConfiguration, TextBox obj) {
        if (equals(obj))
            return true;
        return globalConfiguration.getWordSimUtils().getSimilarity(text, obj.text) > SIMILARITY_THRESHOLD && boundingBox.similar(globalConfiguration,
                obj.boundingBox);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TextBox other) {
            return boundingBox.equals(other.boundingBox) && text.equals(other.text);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boundingBox, text);
    }
}
