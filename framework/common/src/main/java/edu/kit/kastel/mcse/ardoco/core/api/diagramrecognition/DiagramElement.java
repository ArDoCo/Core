package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import java.awt.*;
import java.util.*;
import org.jetbrains.annotations.NotNull;

public abstract class DiagramElement extends Entity implements Comparable<DiagramElement> {
  private Diagram diagram;
  private Color primaryColor;

  protected DiagramElement(@NotNull Diagram diagram, @NotNull String name,
                           Color primaryColor) {
    super(name);
    this.diagram = diagram;
    this.primaryColor = primaryColor;
  }

  protected DiagramElement() {
    super(UUID.randomUUID().toString());
  }

  /**
   * Returns a {@link BoundingBox}, which encases the element.
   *
   * @return the {@link BoundingBox}
   */
  public abstract @NotNull BoundingBox getBoundingBox();

  /**
   * Returns the {@link Diagram}, which this element belongs to.
   *
   * @return the {@link Diagram}
   */
  public @NotNull Diagram getDiagram() {
    return this.diagram;
  }

  /**
   * Returns all elements with a bounding box that is entirely contained in this element's
   * bounding box. See
   * {@link BoundingBox#containsEntirely(BoundingBox)}.
   *
   * @return the set of elements which are considered sub elements
   */
  public @NotNull Set<DiagramElement> getSubElements() {
    var all = getDiagram().getBoxes();
    return new LinkedHashSet<>(all.stream().filter(de -> !de.equals(this) && getBoundingBox().containsEntirely(de.getBoundingBox())).toList());
  }

  public @NotNull Optional<Color> getPrimaryColor() {
    return Optional.ofNullable(primaryColor);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DiagramElement other) {
      return Objects.equals(getDiagram().getResourceName(), other.getDiagram().getResourceName()) && getBoundingBox().equals(other.getBoundingBox());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getDiagram(), getBoundingBox());
  }

  @Override
  public int compareTo(@NotNull DiagramElement o) {
    if (equals(o))
      return 0;
    return hashCode() - o.hashCode();
  }
}
