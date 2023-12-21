/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.MissingBoxInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

/**
 * This rule requires that all entities of the model are represented in the diagram.
 */
public class AllModelEntitiesMustBeRepresented extends Rule {

    @Override
    public List<Inconsistency<Box, Entity>> check(Box box, Entity entity) {
        if (box == null && entity != null) {
            return List.of(new MissingBoxInconsistency<>(entity));
        }

        return List.of();
    }
}
