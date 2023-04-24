/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.old.uml;

import edu.kit.kastel.mcse.ardoco.core.models.old.uml.xml_elements.PackagedElement;

abstract class UMLElement {
    protected final PackagedElement element;

    protected final String id;
    protected final String name;

    UMLElement(PackagedElement element) {
        this.element = element;
        this.id = element.getId();
        this.name = element.getName();
    }

    public final String getId() {
        return id;
    }

    public final String getName() {
        return name;
    }
}
