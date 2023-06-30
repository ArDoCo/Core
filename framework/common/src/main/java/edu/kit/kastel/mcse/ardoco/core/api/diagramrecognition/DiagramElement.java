package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

public abstract class DiagramElement extends Entity implements Comparable<DiagramElement> {
    public record BoundingBox(int minX, int minY, int maxX, int maxH) {
    }

    /**
     * Returns a {@link BoundingBox}, which encases the element.
     *
     * @return the {@link BoundingBox}
     */
    public abstract BoundingBox getBoundingBox();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DiagramElement other) {
            return getBoundingBox().equals(other.getBoundingBox());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getBoundingBox().hashCode();
    }

    @Override
    public int compareTo(DiagramElement o) {
        return hashCode() - o.hashCode();
    }
}
