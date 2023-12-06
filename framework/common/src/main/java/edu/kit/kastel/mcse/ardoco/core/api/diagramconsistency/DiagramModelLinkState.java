/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency;

import org.eclipse.collections.api.bimap.MutableBiMap;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * Stores all results of the matching process.
 */
public interface DiagramModelLinkState extends PipelineStepData {
    /**
     * The ID of this state.
     */
    String ID = "DiagramModelLinkState";

    /**
     * Adds a link between a diagram element and a model element.
     *
     * @param modelType
     *                  The model type of the model in which the model element is located.
     * @param diagramID
     *                  The ID of the diagram element.
     * @param modelID
     *                  The ID of the model element.
     */
    void addLink(ModelType modelType, String diagramID, String modelID);

    /**
     * Get all currently stored links between the diagram and a model.
     *
     * @param modelType
     *                  The type of the model.
     * @return The links.
     */
    MutableBiMap<String, String> getLinks(ModelType modelType);
}
