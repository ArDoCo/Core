/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class TextBox implements Serializable {
    @JsonProperty("x")
    private int xCoordinate;
    @JsonProperty("y")
    private int yCoordinate;
    @JsonProperty("w")
    private int width;
    @JsonProperty("h")
    private int height;
    @JsonProperty
    private double confidence;
    @JsonProperty
    private String text;
    private transient Integer dominatingColor;

    private TextBox() {
        // Jackson JSON
    }

    public TextBox(int xCoordinate, int yCoordinate, int width, int height, double confidence, String text, Integer dominatingColor) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        this.width = width;
        this.height = height;
        this.confidence = confidence;
        this.text = text;
        this.dominatingColor = dominatingColor;
    }

    /**
     * Get the coordinates of the absolute box as (x1,y1,x2,y2) in pixel.
     *
     * @return the absolute coordinates of the box
     */
    public int[] absoluteBox() {
        return new int[] { xCoordinate, yCoordinate, xCoordinate + width, yCoordinate + height };
    }

    public int area() {
        return width * height;
    }

    public int getXCoordinate() {
        return xCoordinate;
    }

    public int getYCoordinate() {
        return yCoordinate;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getText() {
        return text;
    }

    public Integer getDominatingColor() {
        return dominatingColor;
    }

    public void setDominatingColor(Integer dominatingColor) {
        this.dominatingColor = dominatingColor;
    }
}
