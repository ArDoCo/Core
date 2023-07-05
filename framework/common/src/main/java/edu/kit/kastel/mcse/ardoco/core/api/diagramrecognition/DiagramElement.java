package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

public abstract class DiagramElement extends Entity implements Comparable<DiagramElement> {
    private final Diagram diagram;

    public record BoundingBox(int minX, int minY, int maxX, int maxH) {
    }

    protected DiagramElement(@NotNull Diagram diagram, @NotNull String name) {
        super(name);
        this.diagram = diagram;
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiagramElement other) {
            try {
                return Files.isSameFile(getDiagram().getLocation().toPath(), other.getDiagram().getLocation().toPath()) && getBoundingBox().equals(
                        other.getBoundingBox());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getDiagram(), getBoundingBox());
    }

    @Override
    public int compareTo(DiagramElement o) {
        return hashCode() - o.hashCode();
    }
}
