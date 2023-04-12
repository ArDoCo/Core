/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.uml.xml_elements;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass
public final class Reference {
    @XMLValue(name = "xmi:id")
    private String id;
    @XMLValue
    private String client;
    @XMLValue
    private String supplier;

    public String getId() {
        return id;
    }

    public String getClient() {
        return client;
    }

    public String getSupplier() {
        return supplier;
    }
}
