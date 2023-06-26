package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

public abstract class DiagramElement extends Entity {
    public record BoundingBox(int minX, int minY, int maxX, int maxH) {
    }

    /**
     * Returns a {@link BoundingBox}, which encases the element.
     *
     * @return the {@link BoundingBox}
     */
    public abstract BoundingBox getBoundingBox();

    /**
     * Diagram this element belongs to
     *
     * @return the {@link Diagram}
     */
    public abstract Diagram getDiagram();

    @Override
    public int hashCode() {
        return getBoundingBox().hashCode();
    }
}
