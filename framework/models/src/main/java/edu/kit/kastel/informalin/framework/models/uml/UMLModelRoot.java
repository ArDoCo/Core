/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.models.uml;

import java.util.List;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

import edu.kit.kastel.informalin.framework.models.uml.xml_elements.PackagedElement;

@XMLClass(name = "uml:Model")
public final class UMLModelRoot {
    @XMLValue(name = "xmi:id")
    private String id;
    @XMLList(name = "packagedElement", elementType = PackagedElement.class)
    private List<PackagedElement> interfacesAndComponents;

    private List<UMLComponent> components;

    private List<UMLInterface> interfaces;

    public void init() {
        this.interfaces = loadInterfaces();
        this.components = loadComponents();
        this.components.forEach(umlComponent -> umlComponent.init(this));
    }

    private List<UMLComponent> loadComponents() {
        return interfacesAndComponents.stream().filter(PackagedElement::isComponent).map(UMLComponent::new).toList();
    }

    private List<UMLInterface> loadInterfaces() {
        return interfacesAndComponents.stream().filter(PackagedElement::isInterface).map(UMLInterface::new).toList();
    }

    public List<UMLComponent> getComponents() {
        return components;
    }

    public List<UMLInterface> getInterfaces() {
        return interfaces;
    }

    public String getId() {
        return id;
    }
}
