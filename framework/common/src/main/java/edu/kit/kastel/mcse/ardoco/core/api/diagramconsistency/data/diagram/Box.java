package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.data.diagram;

import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A box that is part of a {@link Diagram}.
 */
@Deterministic public final class Box {
    private final int id;
    private final Set<Line> outgoingLines = new LinkedHashSet<>();
    private final Set<Box> containedBoxes = new LinkedHashSet<>();
    private String text;

    /**
     * Creates a new box.
     *
     * @param id
     *         The id of the box. Must be unique in a diagram.
     * @param text
     *         The text of the box.
     */
    public Box(int id, String text) {
        this.id = id;
        this.text = text;
    }

    /**
     * Gets the id of the box.
     *
     * @return The id.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Gets the text of the box.
     *
     * @return The text. May be empty.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Changes the text of the box.
     *
     * @param text
     *         The new text. May be empty.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Adds a new line from this box to another box.
     *
     * @param box
     *         The other box.
     */
    public void addLineTo(Box box) {
        if (box.equals(this)) {
            throw new IllegalArgumentException("Box cannot be connected to itself");
        }

        this.outgoingLines.add(new Line(this, box));
    }

    /**
     * Removes a line from this box to another box. If the line does not exist, nothing happens.
     *
     * @param box
     *         The other box.
     */
    public void removeLineTo(Box box) {
        this.outgoingLines.removeIf(line -> line.target()
                .equals(box));
    }

    /**
     * Adds a box to the contained boxes of this box.
     *
     * @param box
     *         The box to add.
     */
    public void addContainedBox(Box box) {
        if (box.equals(this)) {
            throw new IllegalArgumentException("Box cannot contain itself");
        }

        this.containedBoxes.add(box);
    }

    /**
     * Removes a box from the contained boxes of this box.
     *
     * @param box
     *         The box to remove.
     */
    public void removeContainedBox(Box box) {
        this.containedBoxes.remove(box);
    }

    /**
     * Gets the lines that are outgoing from this box.
     *
     * @return The outgoing lines. May be empty.
     */
    public Set<Line> getOutgoingLines() {
        return Collections.unmodifiableSet(this.outgoingLines);
    }

    /**
     * Checks whether this box has a line to another box.
     *
     * @param box
     *         The potential target box.
     * @return Whether this box has a line to the other box.
     */
    public boolean hasLineTo(Box box) {
        return this.outgoingLines.stream()
                .anyMatch(line -> line.target()
                        .equals(box));
    }

    /**
     * Gets the boxes that are contained in this box.
     *
     * @return The contained boxes. May be empty.
     */
    public Set<Box> getContainedBoxes() {
        return Collections.unmodifiableSet(this.containedBoxes);
    }

    /**
     * Gets all boxes that are contained in this box, including boxes contained in contained boxes and so on.
     *
     * @return The contained boxes. May be empty.
     */
    public Set<Box> getAllContainedBoxes() {
        Set<Box> allContainedBoxes = new LinkedHashSet<>();

        for (Box box : this.containedBoxes) {
            allContainedBoxes.add(box);
            allContainedBoxes.addAll(box.getAllContainedBoxes());
        }

        return allContainedBoxes;
    }

    @Override
    public String toString() {
        return "Box '" + this.text + "' (" + this.id + ")";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || this.getClass() != object.getClass())
            return false;
        Box box = (Box) object;
        return this.getId() == box.getId() && Objects.equals(this.getText(), box.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId(), this.getText());
    }
}
