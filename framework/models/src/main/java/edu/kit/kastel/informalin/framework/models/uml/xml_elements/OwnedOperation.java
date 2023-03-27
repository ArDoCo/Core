/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.models.uml.xml_elements;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass
public final class OwnedOperation {
    @XMLValue(name = "xmi:id")
    private String id;
    @XMLValue
    private String name;
}
