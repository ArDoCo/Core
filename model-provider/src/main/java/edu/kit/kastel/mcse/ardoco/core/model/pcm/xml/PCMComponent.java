/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.model.pcm.xml;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass
public final class PCMComponent {
    @XMLValue
    private String id;
    @XMLValue
    private String entityName;

    @XMLValue(name = "xsi:type")
    private String type;

    public PCMComponent() {
        // NOP
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getType() {
        // e.g., xsi:type="repository:BasicComponent"
        return type.split(":")[1];
    }
}
