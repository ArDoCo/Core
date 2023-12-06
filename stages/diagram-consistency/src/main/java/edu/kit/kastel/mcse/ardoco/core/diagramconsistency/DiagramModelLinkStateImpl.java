/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency;

import java.util.Map;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramModelLinkState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;

/**
 * Implementation of {@link DiagramModelLinkState}.
 */
public class DiagramModelLinkStateImpl implements DiagramModelLinkState {

    private final Map<String, MutableBiMap<String, String>> links = new HashBiMap<>();

    @Override
    public void addLink(ModelType modelType, String diagramID, String modelID) {
        this.links.computeIfAbsent(modelType.getModelId(), k -> new HashBiMap<>()).put(diagramID, modelID);
    }

    @Override
    public MutableBiMap<String, String> getLinks(ModelType modelType) {
        return this.links.getOrDefault(modelType.getModelId(), new HashBiMap<>());
    }

    /**
     * Set the links for a given model.
     *
     * @param modelType
     *                  The model type to set the links for.
     * @param links
     *                  The links to set. Will overwrite existing links.
     */
    public void setLinks(ModelType modelType, MutableBiMap<String, String> links) {
        this.links.put(modelType.getModelId(), links);
    }
}
