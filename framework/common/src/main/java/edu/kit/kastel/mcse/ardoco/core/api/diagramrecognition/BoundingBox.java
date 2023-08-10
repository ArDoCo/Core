package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;
import java.util.Optional;

import org.jetbrains.annotations.NotNull;

public record BoundingBox(int minX, int minY, int maxX, int maxY) implements Serializable {
    public Optional<BoundingBox> intersect(BoundingBox other) {
        if (minX() > other.maxX() || maxX() < other.minX() || minY() > other.maxY() || maxY() < other.minY())
            return Optional.empty();
        return Optional.of(new BoundingBox(Math.max(minX(), other.minX()), Math.max(minY(), other.minY()), Math.min(maxX(), other.maxX()),
                Math.min(maxY(), other.maxY())));
    }

    public double union(BoundingBox other) {
        var i = intersect(other).map(BoundingBox::area).orElse(0.0);
        return area() + other.area() - i;
    }

    public double area() {
        return (maxX() - minX()) * (maxY() - minY());
    }

    public double intersectionOverUnion(BoundingBox other) {
        return intersect(other).map(i -> i.area() / union(other)).orElse(0.0);
    }

    public double contains(BoundingBox other) {
        return contains(other, false);
    }

    /**
     * How much of another bounding box is contained in this instance. If the considerArea flag is set, a smaller box can not contain a larger box. Returns a
     * value in the range [0,1] with 1 representing an entirely contained box.
     *
     * @param other        a {@link BoundingBox}
     * @param considerArea whether the area of both boxes should be considered
     * @return
     */
    public double contains(BoundingBox other, boolean considerArea) {
        if (considerArea && other.area() > area())
            return 0.0;
        return intersect(other).map(i -> i.area() / other.area()).orElse(0.0);
    }

    /**
     * Whether this instance contains another bounding box entirely.
     *
     * @param other a {@link BoundingBox}
     * @return true if contained entirely, false otherwise
     */
    public boolean containsEntirely(@NotNull BoundingBox other) {
        return Double.compare(contains(other, true), 1.0) == 0;
    }

    /**
     * {@return the width}
     */
    public int width() {
        return maxX - minX;
    }

    /**
     * {@return the height}
     */
    public int height() {
        return maxY - minY;
    }

    public int[] toCoordinates() {
        return new int[] { minX, minY, maxX, maxY };
    }
}
