/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.*;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;
import org.jetbrains.annotations.NotNull;

import static java.lang.Math.abs;

/**
 * This class represents a box that is detected by the image recognition.
 */
public class Box extends DiagramElement implements Serializable {
  @JsonProperty
  private String uuid;
  // the four coordinates x1,y1,x2,y2
  @JsonProperty("box")
  private int[] coordinates;
  @JsonProperty
  private double confidence;
  @JsonProperty("class")
  private String classification;
  private List<TextBox> textBoxes = new ArrayList<>();
  private Integer dominatingColor = null;
  @JsonIgnore
  private MutableSet<String> references = Sets.mutable.empty();

  // Jackson JSON
  private Box() {
    super();
  }

  /**
   * Create a new box that is detected on the image.
   *
   * @param uuid            a unique identifier
   * @param coordinates     the coordinates of two corners of the box in pixel. (x1,y1,x2,y2)
   * @param confidence      a confidence value
   * @param classification  the classification (e.g., "LABEL"), see {@link Classification} for
   *                        further details
   * @param textBoxes       the text boxes that are attached to this box
   * @param dominatingColor a dominating color in the box (iff present)
   */
  public Box(@NotNull Diagram diagram, String uuid, int[] coordinates, double confidence,
             String classification, List<TextBox> textBoxes,
             Integer dominatingColor) {
    super(diagram, uuid, dominatingColor != null ? new Color(dominatingColor) : null);
    this.uuid = uuid;
    this.coordinates = coordinates;
    this.confidence = confidence;
    this.classification = classification;
    this.textBoxes = textBoxes;
    this.dominatingColor = dominatingColor;
  }

  //TODO violates the constructor contract, for compatibility as of now
  public Box(String uuid, int[] coordinates, double confidence, String classification,
             List<TextBox> textBoxes, Integer dominatingColor) {
    super();
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
   * Get all text boxes that are associated with the box.
   *
   * @return all associated text boxes
   */
  public List<TextBox> getTexts() {
    return new ArrayList<>(textBoxes);
  }

  public Set<String> getReferences() {
    return Collections.unmodifiableSet(references);
  }

  public boolean addReference(String reference) {
    return references.add(reference);
  }

  public void setReferences(Set<String> references) {
    this.references = Sets.mutable.ofAll(references);
  }

  public boolean removeReference(String reference) {
    return references.remove(reference);
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

  @NotNull
  @Override
  public BoundingBox getBoundingBox() {
    return new BoundingBox(coordinates[0], coordinates[1], coordinates[2], coordinates[3]);
  }

  @Override
  public String toString() {
    return uuid;
  }
}
