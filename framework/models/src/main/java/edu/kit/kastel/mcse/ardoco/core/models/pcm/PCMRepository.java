/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.pcm;

import java.util.ArrayList;
import java.util.List;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass(name = "repository:Repository")
public final class PCMRepository {
    @XMLValue
    private String id;
    @XMLValue(mandatory = false)
    private String entityName;
    @XMLList(name = "components__Repository", elementType = PCMComponent.class)
    private List<PCMComponent> components;

    @XMLList(name = "interfaces__Repository", elementType = PCMInterface.class)
    private List<PCMInterface> interfaces;

    PCMRepository() {
        // NOP
    }

    void init() {
        this.components.forEach(c -> c.init(this.interfaces));
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public List<PCMComponent> getComponents() {
        return new ArrayList<>(components);
    }

    public List<PCMInterface> getInterfaces() {
        return new ArrayList<>(interfaces);
    }
}
