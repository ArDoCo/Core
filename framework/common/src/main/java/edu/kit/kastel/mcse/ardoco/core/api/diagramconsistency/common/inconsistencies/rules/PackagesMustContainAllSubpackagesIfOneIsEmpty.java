/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.DiagramUtility;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Transformations;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.MissingBoxInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * This rule requires that a box that represents a package contains all its subpackages if one of the subpackages is
 * displayed without content despite having content in the model.
 */
@Deterministic
public class PackagesMustContainAllSubpackagesIfOneIsEmpty extends Rule {
    private Map<Entity, Set<Entity>> packageToSubpackages = null;
    private SortedMap<String, Box> boxes = null;

    @Override
    public Runnable setup() {
        this.packageToSubpackages = new LinkedHashMap<>();
        Transformations.transformAny(this.getModel(), entity -> {
            if (entity instanceof CodePackage codePackage) {
                this.packageToSubpackages.put(codePackage, new LinkedHashSet<>());
            }
            return entity;
        }, (dependent, dependency) -> {
        }, (child, parent) -> {
            if (child instanceof CodePackage childPackage && parent instanceof CodePackage parentPackage) {
                this.packageToSubpackages.get(parentPackage).add(childPackage);
            }
        });

        this.boxes = DiagramUtility.getBoxes(this.getDiagram());

        return () -> {
            this.packageToSubpackages = null;
            this.boxes = null;
        };
    }

    @Override
    public List<Inconsistency<Box, Entity>> check(Box box, Entity entity) {
        if (box == null || entity == null) {
            return List.of();
        }

        Set<Entity> childEntities = this.packageToSubpackages.get(entity);

        if (childEntities == null || childEntities.isEmpty()) {
            return List.of();
        }

        List<Box> childBoxes = DiagramUtility.getContainedBoxes(box, this.boxes);
        boolean isDisplaying = childBoxes.stream().anyMatch(this::isDisplaying);

        if (!isDisplaying) {
            return List.of();
        }

        List<Inconsistency<Box, Entity>> inconsistencies = new ArrayList<>();
        for (Entity childEntity : childEntities) {
            Box childBox = this.getLinks().inverse().get(childEntity);
            if (childBox == null) {
                inconsistencies.add(new MissingBoxInconsistency<>(childEntity));
            }
        }
        return inconsistencies;
    }

    private boolean isDisplaying(Box box) {
        Entity entity = this.getLinks().get(box);
        if (entity == null) {
            return false;
        }
        if (!this.packageToSubpackages.containsKey(entity)) {
            return false;
        }
        return box.getContainedBoxes().isEmpty();
    }
}
