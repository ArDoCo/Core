/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage3;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.DiagramUtility;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.*;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

/**
 * Represents an inconsistency between the diagram and the model.
 * This class describes a simplified version for serialization.
 * Specifically, the ease of writing the JSON by hand was a goal.
 *
 * @param type
 *                       The type of the inconsistency.
 * @param boxId
 *                       The id of the diagram element. Is not set when the type is {@link InconsistencyType#MISSING_BOX}.
 * @param modelElementId
 *                       The id of the model element. Is not set when the type is {@link InconsistencyType#UNEXPECTED_BOX}.
 * @param line
 *                       Describes a line. Only set if consistency type is {@link InconsistencyType#MISSING_LINE} or
 *                       {@link InconsistencyType#UNEXPECTED_LINE}.
 * @param name
 *                       Further information in case the type is {@link InconsistencyType#NAME_INCONSISTENCY}.
 */
public record DiagramInconsistency(@JsonProperty("type") InconsistencyType type, @JsonProperty("diagram_id") Integer boxId,
                                   @JsonProperty("model_id") String modelElementId, @JsonProperty("line_id") int[] line,
                                   @JsonProperty("name_inconsistency") NameInformation name,
                                   @JsonProperty("hierarchy_inconsistency") HierarchyInformation hierarchy) {
    /**
     * Provides further information in case the type is {@link InconsistencyType#NAME_INCONSISTENCY}.
     *
     * @param expected
     *                 The expected name.
     */
    public record NameInformation(@JsonProperty("expected") String expected) {
    }

    /**
     * Converts this data transfer object to the inconsistency class used by the logic of the pipeline. This step also
     * resolves all IDs to the actual objects.
     *
     * @param diagram
     *                The diagram, providing the boxes.
     * @param model
     *                The model, providing the entities.
     * @return The inconsistency.
     */
    public Inconsistency<Box, Entity> toInconsistency(Map<String, Box> diagram, Map<String, Entity> model) {
        Box box = null;
        if (this.boxId != null) {
            box = diagram.get(String.valueOf(this.boxId));
        }
        Entity entity = null;
        if (this.modelElementId != null) {
            entity = model.get(this.modelElementId);
        }
        return switch (this.type) {
        case MISSING_BOX -> {
            Objects.requireNonNull(entity);
            yield new MissingBoxInconsistency<>(entity);
        }
        case UNEXPECTED_BOX -> {
            Objects.requireNonNull(box);
            yield new UnexpectedBoxInconsistency<>(box);
        }
        case NAME_INCONSISTENCY -> {
            Objects.requireNonNull(box);
            Objects.requireNonNull(entity);
            Objects.requireNonNull(this.name);
            yield new NameInconsistency<>(box, entity, this.name.expected, DiagramUtility.getBoxText(box));
        }
        case HIERARCHY_INCONSISTENCY -> {
            Objects.requireNonNull(box);
            Objects.requireNonNull(entity);
            Box parent = null;
            if (this.hierarchy != null) {
                parent = diagram.get(String.valueOf(this.hierarchy.parentBoxId));
            }
            yield new HierarchyInconsistency<>(box, entity, parent);
        }
        case MISSING_LINE -> {
            Objects.requireNonNull(this.line);
            Box start = diagram.get(String.valueOf(this.line[0]));
            Box end = diagram.get(String.valueOf(this.line[1]));
            yield new MissingLineInconsistency<>(start, end);
        }
        case UNEXPECTED_LINE -> {
            Objects.requireNonNull(this.line);
            Box start = diagram.get(String.valueOf(this.line[0]));
            Box end = diagram.get(String.valueOf(this.line[1]));
            yield new UnexpectedLineInconsistency<>(start, end);
        }
        };
    }

    /**
     * Provides further information in case the type is {@link InconsistencyType#HIERARCHY_INCONSISTENCY}.
     *
     * @param parentBoxId
     *                    The box id of the expected parent.
     */
    public record HierarchyInformation(@JsonProperty("parent") int parentBoxId) {
    }
}
