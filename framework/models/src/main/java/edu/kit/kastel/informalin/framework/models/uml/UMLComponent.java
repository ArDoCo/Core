/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.models.uml;

import java.util.List;

import edu.kit.kastel.informalin.framework.models.uml.xml_elements.PackagedElement;

public class UMLComponent extends UMLElement {
    private List<UMLInterface> requiredInterfaces;
    private List<UMLInterface> providedInterfaces;

    UMLComponent(PackagedElement componentElement) {
        super(componentElement);
    }

    void init(UMLModelRoot umlModelRoot) {
        this.requiredInterfaces = loadRequired(umlModelRoot);
        this.providedInterfaces = loadProvided(umlModelRoot);
    }

    private List<UMLInterface> loadRequired(UMLModelRoot umlModelRoot) {
        var usages = element.getUsages();
        if (usages == null)
            return List.of();
        return usages.stream().map(usage -> findById(umlModelRoot, usage.getSupplier())).toList();
    }

    private List<UMLInterface> loadProvided(UMLModelRoot umlModelRoot) {
        var realizations = element.getInterfaceRealizations();
        if (realizations == null)
            return List.of();
        return realizations.stream().map(realization -> findById(umlModelRoot, realization.getSupplier())).toList();
    }

    private static UMLInterface findById(UMLModelRoot umlModelRoot, String id) {
        return umlModelRoot.getInterfaces()
                .stream()
                .filter(it -> it.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not find interface with UMLId " + id));
    }

    public List<UMLInterface> getRequired() {
        return requiredInterfaces;
    }

    public List<UMLInterface> getProvided() {
        return providedInterfaces;
    }

}
