package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.NameInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.data.diagram.Box;
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

        if (box.getText()
                .equals(entity.getName())) {
            return List.of();
        }

        return List.of(new NameInconsistency<>(box, entity, entity.getName(), box.getText()));
    }
}
