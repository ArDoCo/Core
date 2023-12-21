/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.UnexpectedBoxInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

/**
 * This rule requires that all boxes are part of a link.
 */
public class AllBoxesMustBeLinked extends Rule {
    @Override
    public List<Inconsistency<Box, Entity>> check(Box box, Entity entity) {
        if (box != null && entity == null) {
            return List.of(new UnexpectedBoxInconsistency<>(box));
        }

        return List.of();
    }
}
