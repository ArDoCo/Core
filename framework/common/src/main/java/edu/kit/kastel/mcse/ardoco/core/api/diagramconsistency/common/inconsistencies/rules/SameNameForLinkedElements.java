/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.DiagramUtility;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.NameInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

/**
 * This rule requires that linked elements have the same name.
 */
public class SameNameForLinkedElements extends Rule {
    @Override
    public List<Inconsistency<Box, Entity>> check(Box box, Entity entity) {
        if (box == null || entity == null) {
            return List.of();
        }

        if (DiagramUtility.getBoxText(box).equals(entity.getName())) {
            return List.of();
        }

        return List.of(new NameInconsistency<>(box, entity, entity.getName(), DiagramUtility.getBoxText(box)));
    }
}
