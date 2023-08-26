package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelElement;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityComparable;

public abstract class DiagramElement extends Entity implements SimilarityComparable<DiagramElement> {
    private Diagram diagram;

    protected DiagramElement(@NotNull Diagram diagram, @NotNull String uuid) {
        super(uuid);
        this.diagram = diagram;
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
     * Returns all elements with a bounding box that is entirely contained in this element's bounding box. See
     * {@link BoundingBox#containsEntirely(BoundingBox)}.
     *
     * @return the set of elements which are considered sub elements
     */
    public @NotNull Set<DiagramElement> getSubElements() {
        var all = getDiagram().getBoxes();
        return new LinkedHashSet<>(all.stream().filter(de -> !de.equals(this) && getBoundingBox().containsEntirely(de.getBoundingBox())).toList());
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
        return Objects.hash(getDiagram().getResourceName(), getBoundingBox());
    }

    @Override
    public int compareTo(@NotNull ModelElement o) {
        if (equals(o))
            return 0;
        if (o instanceof DiagramElement other) {
            return hashCode() - other.hashCode();
        }
        return super.compareTo(o);
    }

    @Override
    public boolean similar(DiagramElement obj) {
        if (equals(obj))
            return true;
        if (diagram.getResourceName().equals(obj.diagram.getResourceName()))
            return getBoundingBox().similar(obj.getBoundingBox());
        return false;
    }
}
