/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.old.pcm;

import java.util.ArrayList;
import java.util.List;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass
public final class PCMInterface {
    @XMLValue
    private String id;
    @XMLValue
    private String entityName;
    @XMLList(name = "signatures__OperationInterface", elementType = PCMSignature.class)
    private List<PCMSignature> methods;

    PCMInterface() {
        // NOP
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public List<PCMSignature> getMethods() {
        return new ArrayList<>(methods);
    }
}
