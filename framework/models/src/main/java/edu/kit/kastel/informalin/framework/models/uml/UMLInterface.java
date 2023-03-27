/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.models.uml;

import java.util.List;

import edu.kit.kastel.informalin.framework.models.uml.xml_elements.OwnedOperation;
import edu.kit.kastel.informalin.framework.models.uml.xml_elements.PackagedElement;

public class UMLInterface extends UMLElement {
    private final List<OwnedOperation> operations;

    UMLInterface(PackagedElement interfaceElement) {
        super(interfaceElement);
        this.operations = interfaceElement.getOwnedOperations().stream().toList();
    }

    public List<OwnedOperation> getOperations() {
        return operations;
    }
}
