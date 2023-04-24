package edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.architecture.uml.parser;

import edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.architecture.uml.parser.xmlelements.PackagedElement;

abstract class UmlElement {

    protected final PackagedElement element;

    protected final String id;
    protected final String name;

    UmlElement(PackagedElement element) {
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
