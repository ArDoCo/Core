/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data.stage2;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * All links between the diagram elements and the model elements.
 *
 * @param name
 *              The name of the diagram.
 * @param links
 *              The links between the diagram elements and the model elements.
 */
public record ElementLinks(@JsonProperty("name") String name, @JsonProperty("links") Link[] links) {
    /**
     * Converts the links to a bidirectional map.
     *
     * @return The bidirectional map.
     */
    public MutableBiMap<String, String> toBiMap() {
        HashBiMap<String, String> biMap = org.eclipse.collections.impl.bimap.mutable.HashBiMap.newMap();
        for (var link : this.links) {
            biMap.put(String.valueOf(link.boxId()), link.modelElementId());
        }
        return biMap;
    }
}
