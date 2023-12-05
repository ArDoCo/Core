package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.DiagramUtility;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Connector;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.MissingLineInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.UnexpectedLineInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Transformations;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * This rule requires that every box is connected to all its dependencies and no other boxes. Every provider of a
 * required interface is a dependency.
 */
@Deterministic public class EntitiesMustBeConnectedExactlyToDependencies extends Rule {
    private Map<Entity, Set<Entity>> entityToDependencies = null;

    @Override
    public Runnable setup() {
        this.entityToDependencies = new LinkedHashMap<>();
        Transformations.transformAny(this.getModel(), entity -> {
            this.entityToDependencies.put(entity, new LinkedHashSet<>());
            return entity;
        }, (dependent, dependency) -> this.entityToDependencies.get(dependent)
                .add(dependency), (child, parent) -> {
        });

        return () -> this.entityToDependencies = null;
    }

    @Override
    public List<Inconsistency<Box, Entity>> check(Box box, Entity entity) {
        if (box == null || entity == null) {
            return List.of();
        }

        Set<Entity> dependencies = this.entityToDependencies.get(entity);
        List<Inconsistency<Box, Entity>> inconsistencies = new ArrayList<>();

        for (Entity dependency : dependencies) {
            Box dependencyBox = this.getLinks()
                    .inverse()
                    .get(dependency);
            if (dependencyBox != null && !DiagramUtility.hasConnectionBetween(this.getDiagram(), box, dependencyBox)) {
                inconsistencies.add(new MissingLineInconsistency<>(box, dependencyBox));
            }
        }

        SortedMap<String, Box> boxes = DiagramUtility.getBoxes(this.getDiagram());
        for (Connector connector : DiagramUtility.getOutgoingConnectors(getDiagram(), box)) {
            for (Box target : DiagramUtility.getTargets(connector, boxes)) {
                Entity targetEntity = this.getLinks()
                        .get(target);
                if (targetEntity != null && !dependencies.contains(targetEntity)) {
                    inconsistencies.add(new UnexpectedLineInconsistency<>(box, target));
                }
            }
        }

        return inconsistencies;
    }
}
