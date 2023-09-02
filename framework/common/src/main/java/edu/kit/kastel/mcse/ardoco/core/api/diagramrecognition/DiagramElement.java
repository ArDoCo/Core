package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.impl.factory.Sets;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelElement;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityComparable;

public abstract class DiagramElement extends Entity implements SimilarityComparable<DiagramElement> {
    /**
     * Required for null object pattern
     */
    private static class NullObject extends DiagramElement {
        @NotNull
        @Override
        public BoundingBox getBoundingBox() {
            throw new UnsupportedOperationException();
        }
    }

    private Diagram diagram;

    /**
     * Null if uninitialized NullObject if no parent exists
     */
    private DiagramElement parent;

    private transient MutableSet<DiagramElement> children;

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
     * {@return the set of elements which are considered direct children}
     */
    public @NotNull ImmutableSet<DiagramElement> getChildren() {
        if (children == null) {
            var all = getDiagram().getBoxes();
            children = Sets.mutable.fromStream(all.stream().filter(de -> !de.equals(this) && de.getParent().map(p -> p == this).orElse(false)));
        }
        return children.toImmutable();
    }

    public Optional<DiagramElement> getParent() {
        if (parent == null) {
            var all = getDiagram().getBoxes();
            parent = all.stream().filter(de -> !de.equals(this) && de.getBoundingBox().containsEntirely(getBoundingBox())) //Find boxes containing this element
                    .min(Comparator.comparingDouble(de -> de.getBoundingBox().area())) //Find smallest box containing this element
                    .map(de -> (DiagramElement) de).orElse(new NullObject()); //Cast to diagram element

        }
        if (parent instanceof NullObject)
            return Optional.empty();
        return Optional.of(parent);
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
