/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage3;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.DiagramUtility;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

/**
 * Inconsistencies between a specific diagram and the represented model.
 * This class describes a simplified version for serialization.
 *
 * @param name
 *                        The name of the diagram.
 * @param inconsistencies
 *                        The inconsistencies.
 */
public record DiagramInconsistencies(@JsonProperty("name") String name, @JsonProperty("inconsistencies") DiagramInconsistency[] inconsistencies) {

    /**
     * Convert in which class the inconsistencies are stored to the class used for pipeline logic. This step also
     * resolves all IDs to the actual objects.
     *
     * @param diagram
     *                The diagram, providing the boxes.
     * @param model
     *                The model, providing the entities.
     * @return The inconsistencies.
     */
    public List<Inconsistency<Box, Entity>> toInconsistencies(Diagram diagram, Map<String, Entity> model) {
        return Arrays.stream(this.inconsistencies).map(inconsistency -> inconsistency.toInconsistency(DiagramUtility.getBoxes(diagram), model)).toList();
    }
}
