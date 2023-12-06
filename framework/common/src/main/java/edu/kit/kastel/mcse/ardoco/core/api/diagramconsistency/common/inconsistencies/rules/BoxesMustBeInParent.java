/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.DiagramUtility;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Transformations;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.HierarchyInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * This rule requires that every box is contained in the box corresponding to its parent entity. If the parent is not
 * present in the diagram, this rule does not apply. Additionally, this rule checks that boxes do not have a parent box
 * that is not their parent entity.
 */
@Deterministic
public class BoxesMustBeInParent extends Rule {
    private Map<Entity, Entity> entityToParent = null;
    private Map<Box, Box> boxToParent = null;

    @Override
    public Runnable setup() {
        this.entityToParent = new LinkedHashMap<>();
        Transformations.transformAny(this.getModel(), entity -> entity, (dependent, dependency) -> {
        }, (child, parent) -> this.entityToParent.put(child, parent));

        this.boxToParent = new LinkedHashMap<>();
        SortedMap<String, Box> boxes = DiagramUtility.getBoxes(this.getDiagram());
        for (Box parent : this.getDiagram().getBoxes()) {
            for (Box child : DiagramUtility.getContainedBoxes(parent, boxes)) {
                this.boxToParent.put(child, parent);
            }
        }

        return () -> {
            this.entityToParent = null;
            this.boxToParent = null;
        };
    }

    @Override
    public List<Inconsistency<Box, Entity>> check(Box box, Entity entity) {
        if (box == null || entity == null) {
            return List.of();
        }

        Box actualParentBox = this.boxToParent.get(box);
        Entity actualParentEntity = null;
        if (actualParentBox != null) {
            actualParentEntity = this.getLinks().get(actualParentBox);
        }

        Entity expectedParentEntity = this.entityToParent.get(entity);
        Box expectedParentBox = null;
        if (expectedParentEntity != null) {
            expectedParentBox = this.getLinks().inverse().get(expectedParentEntity);
        }

        if (Objects.equals(expectedParentBox, actualParentBox)) {
            return List.of();
        }

        if (actualParentEntity == null) {
            return List.of(); // Actual parent is not linked to model, might be correct parent if link would exist.
        }

        if (expectedParentBox == null && expectedParentEntity != null) {
            return List.of(); // There is an entity that should be parent but is not found in the diagram.
        }

        return List.of(new HierarchyInconsistency<>(box, entity, expectedParentBox));
    }
}
