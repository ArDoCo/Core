/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.models.pcm;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass
public final class PCMSignature {
    @XMLValue
    private String id;
    @XMLValue
    private String entityName;

    PCMSignature() {
        // NOP
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }
}
