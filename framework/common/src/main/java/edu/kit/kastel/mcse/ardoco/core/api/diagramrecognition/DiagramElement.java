/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.impl.factory.SortedSets;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureEntity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelElement;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityComparable;
import edu.kit.kastel.mcse.ardoco.core.data.GlobalConfiguration;

/**
 * This box represents a geometrical shape with an arbitrary amount of text from a diagram. An element can be uniquely identified by its bounding box or UID and
 * the diagram it belongs to.
 */
public abstract class DiagramElement extends ArchitectureEntity implements SimilarityComparable<DiagramElement> {
    private final Diagram diagram;
    private DiagramElement parent;
    private List<DiagramElement> children;

    /**
     * Creates a new diagram element that is associated with the given diagram and unique identifier.
     *
     * @param diagram the diagram this element is associated with
     * @param uuid    the unique identifier
     */
    protected DiagramElement(Diagram diagram, String uuid) {
        super(uuid);
        this.diagram = diagram;
    }

    /**
     * Returns a {@link BoundingBox}, which encases the element.
     *
     * @return the {@link BoundingBox}
     */
    public abstract BoundingBox getBoundingBox();

    /**
     * Returns the {@link Diagram}, which this element belongs to.
     *
     * @return the {@link Diagram}
     */
    public Diagram getDiagram() {
        return this.diagram;
    }

    /**
     * {@return the set of elements which are direct children of this diagram element} Determined indirectly by searching for diagram elements in the diagram
     * which reference this element as their parent.
     *
     * @see #getParent()
     */
    public ImmutableSortedSet<DiagramElement> getChildren() {
        if (children == null) {
            var all = getDiagram().getBoxes();
            this.children = new ArrayList<>(all.stream()
                    .filter(de -> !de.equals(DiagramElement.this) && de.getParent().map(p -> p == DiagramElement.this).orElse(false))
                    .map(b -> (DiagramElement) b)
                    .toList());
        }
        return SortedSets.immutable.withAll(children);
    }

    /**
     * {@return the optional parent of this element, empty if this diagram element is at the top-most level} Searches the diagram for diagram elements whose
     * bounding box entirely contain this element. The diagram element with the smallest area is chosen as parent.
     *
     * @see BoundingBox#containsEntirely(BoundingBox)
     */
    public Optional<DiagramElement> getParent() {
        if (parent == null) {
            var all = getDiagram().getBoxes();
            parent = all.stream()
                    .filter(de -> !de.equals(DiagramElement.this) && de.getBoundingBox().containsEntirely(getBoundingBox())) //Find boxes containing this element
                    .min(Comparator.comparingDouble(de -> de.getBoundingBox().area()))
                    .orElse(null);
        }
        return Optional.ofNullable(parent);
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
    public int compareTo(ModelElement o) {
        if (equals(o))
            return 0;
        if (o instanceof DiagramElement other) {
            return Comparator.comparing(DiagramElement::getDiagram).thenComparing(DiagramElement::getBoundingBox).compare(this, other);
        }
        return super.compareTo(o);
    }

    @Override
    public boolean similar(GlobalConfiguration globalConfiguration, DiagramElement obj) {
        if (equals(obj))
            return true;
        if (diagram.getResourceName().equals(obj.diagram.getResourceName()))
            return getBoundingBox().similar(globalConfiguration, obj.getBoundingBox());
        return false;
    }
}
