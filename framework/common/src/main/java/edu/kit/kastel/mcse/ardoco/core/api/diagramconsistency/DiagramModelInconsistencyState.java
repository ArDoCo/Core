/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * Contains information about found inconsistencies between the diagram and the given models.
 */
public interface DiagramModelInconsistencyState extends PipelineStepData {
    /**
     * The ID of this state.
     */
    String ID = "DiagramModelInconsistencyState";

    /**
     * Adds an inconsistency.
     *
     * @param modelType     The model type to add the inconsistency for.
     * @param inconsistency
     *                      The inconsistency to add.
     */
    void addInconsistency(ModelType modelType, Inconsistency<String, String> inconsistency);

    /**
     * Returns all found inconsistencies.
     *
     * @param modelType The model type to get inconsistencies for.
     * @return All inconsistencies.
     */
    List<Inconsistency<String, String>> getInconsistencies(ModelType modelType);

    /**
     * Set the extended inconsistencies. The extended inconsistency list is based on the basic inconsistency list but a
     * larger selection of more concrete inconsistency types can be used.
     *
     * @param modelType
     *                        The model type to set the inconsistencies for.
     * @param inconsistencies
     *                        The inconsistencies to set.
     */
    void setExtendedInconsistencies(ModelType modelType, List<Inconsistency<String, String>> inconsistencies);

    /**
     * Returns the extended inconsistencies. If no extended inconsistencies are set, the basic inconsistencies are
     * returned.
     *
     * @param modelType
     *                  The model type to get the inconsistencies for.
     * @return The extended inconsistencies.
     */
    List<Inconsistency<String, String>> getExtendedInconsistencies(ModelType modelType);
}
