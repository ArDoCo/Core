/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;

import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityComparable;
import edu.kit.kastel.mcse.ardoco.core.data.MetaData;

/**
 * This record represents a 2-dimensional bounding box spanning from the top left point (minX, minY) to (maxX, maxY) in an image where top left is (0,0).
 *
 * @param minX x coordinate of the left bounding box edge
 * @param minY y coordinate of the top bounding box edge
 * @param maxX x coordinate of the right bounding box edge
 * @param maxY y coordinate of the bottom bounding box edge
 */
public record BoundingBox(int minX, int minY, int maxX, int maxY) implements Comparable<BoundingBox>, SimilarityComparable<BoundingBox>, Serializable {

    /**
     * Threshold used by the IoU to determine whether bounding boxes are similar using {@link #similar(MetaData, BoundingBox)}.
     */
    public static final double SIMILARITY_THRESHOLD = 0.7;

    /**
     * Tries calculating a new bounding box of the intersection between this and another bounding box. If the bounding boxes do not intersect, an empty optional
     * is provided.
     *
     * @param other another bounding box
     * @return the optional bounding box
     */
    public Optional<BoundingBox> intersect(BoundingBox other) {
        if (minX() > other.maxX() || maxX() < other.minX() || minY() > other.maxY() || maxY() < other.minY())
            return Optional.empty();
        return Optional.of(new BoundingBox(Math.max(minX(), other.minX()), Math.max(minY(), other.minY()), Math.min(maxX(), other.maxX()), Math.min(maxY(),
                other.maxY())));
    }

    /**
     * Calculates the area of union between this and another bounding box.
     *
     * @param other another bounding box
     * @return the area >= 0
     */
    public double union(BoundingBox other) {
        var i = intersect(other).map(BoundingBox::area).orElse(0.0);
        return area() + other.area() - i;
    }

    /**
     * Calculates the bounding box of this and another bounding box.
     *
     * @param other another bounding box
     * @return a new bounding box
     */
    public BoundingBox combine(BoundingBox other) {
        return new BoundingBox(Math.min(minX, other.minX), Math.max(maxX, other.maxX), Math.min(minY, other.minY), Math.max(maxY, other.maxY));
    }

    /**
     * @return the area of a bounding box, area &gt;= 0
     */
    public double area() {
        return ((double) maxX() - minX()) * (maxY() - minY());
    }

    /**
     * Calculates the intersection over union metric for this bounding box with another bounding box. This metric is the fraction of the intersection with the
     * union of two bounding boxes. If the bounding boxes are identical, this will produce return 1. If the bounding boxes do not intersect, it will return 0
     * instead. For any other case, a value in the range (0,1) is returned depending on the relative overlap.
     *
     * @param other another bounding box
     * @return iou in the range [0,1]
     */
    public double intersectionOverUnion(BoundingBox other) {
        return intersect(other).map(i -> i.area() / union(other)).orElse(0.0);
    }

    /**
     * The percentage of another bounding box that is contained in this instance.
     *
     * @param other another bounding box
     * @return percentage in the range [0,1]
     */
    public double contains(BoundingBox other) {
        return contains(other, false);
    }

    /**
     * The percentage of another bounding box that is contained in this instance. If the considerArea flag is set, a smaller box can not contain a larger box.
     * Returns a value in the range [0,1] with 1 representing an entirely contained box.
     *
     * @param other        a {@link BoundingBox}
     * @param considerArea whether the area of both boxes should be considered
     * @return percentage in the range [0,1]
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
    public boolean containsEntirely(BoundingBox other) {
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

    /**
     * {@return the bounding box coordinates in the order minX, minY, maxX, maxY}
     */
    public int[] toCoordinates() {
        return new int[] { minX, minY, maxX, maxY };
    }

    @Override
    public boolean similar(MetaData metaData, BoundingBox obj) {
        if (equals(obj))
            return true;
        return intersectionOverUnion(obj) > SIMILARITY_THRESHOLD;
    }

    @Override
    public int compareTo(BoundingBox o) {
        if (equals(o))
            return 0;
        return Comparator.comparing(BoundingBox::minX)
                .thenComparing(BoundingBox::minY)
                .thenComparing(BoundingBox::maxX)
                .thenComparing(BoundingBox::maxY)
                .compare(this, o);
    }
}
