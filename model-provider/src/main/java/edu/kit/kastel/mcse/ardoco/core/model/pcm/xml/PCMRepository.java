/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.model.pcm.xml;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass(name = "repository:Repository")
public final class PCMRepository {
    @XMLValue
    private String id;
    @XMLValue
    private String entityName;
    @XMLList(name = "components__Repository", elementType = PCMComponent.class)
    private List<PCMComponent> components;

    public PCMRepository() {
        // NOP
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public ImmutableList<PCMComponent> getComponents() {
        return Lists.immutable.withAll(components);
    }
}
