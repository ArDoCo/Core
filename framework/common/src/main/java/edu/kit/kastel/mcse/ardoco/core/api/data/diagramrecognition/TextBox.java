/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition;

import static java.lang.Math.abs;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class TextBox implements Serializable {
    @JsonProperty
    private int x;
    @JsonProperty
    private int y;
    @JsonProperty
    private int w;
    @JsonProperty
    private int h;
    @JsonProperty
    private double confidence;
    @JsonProperty
    private String text;
    private transient Integer dominatingColor;

    private TextBox() {
        // Jackson JSON
    }

    public TextBox(int x, int y, int w, int h, double confidence, String text, Integer dominatingColor) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.confidence = confidence;
        this.text = text;
        this.dominatingColor = dominatingColor;
    }

    public int[] absoluteBox() {
        return new int[] { x, y, x + w, y + h };
    }

    public int area() {
        int[] box = absoluteBox();
        return abs(box[0] - box[2]) * abs(box[1] - box[3]);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
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
