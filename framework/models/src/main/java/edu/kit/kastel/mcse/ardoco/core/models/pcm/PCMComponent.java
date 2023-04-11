/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.pcm;

import java.util.ArrayList;
import java.util.List;

import org.fuchss.xmlobjectmapper.annotation.XMLClass;
import org.fuchss.xmlobjectmapper.annotation.XMLList;
import org.fuchss.xmlobjectmapper.annotation.XMLValue;

@XMLClass
public final class PCMComponent {
    @XMLValue
    private String id;
    @XMLValue
    private String entityName;

    @XMLValue(name = "xsi:type")
    private String type;

    @XMLList(name = "requiredRoles_InterfaceRequiringEntity", elementType = InterfaceId.class)
    private List<InterfaceId> requiredInterfaceIds;

    @XMLList(name = "providedRoles_InterfaceProvidingEntity", elementType = InterfaceId.class)
    private List<InterfaceId> providedInterfaceIds;

    private List<PCMInterface> required;
    private List<PCMInterface> provided;

    PCMComponent() {
        // NOP
    }

    public String getId() {
        return id;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getType() {
        // e.g., xsi:type="repository:BasicComponent"
        return type.split(":")[1];
    }

    void init(List<PCMInterface> interfaces) {
        this.provided = providedInterfaceIds.stream()
                .map(interfaceId -> interfaces.stream().filter(it -> it.getId().equals(interfaceId.id())).findFirst().orElseThrow())
                .toList();
        this.required = requiredInterfaceIds.stream()
                .map(interfaceId -> interfaces.stream().filter(it -> it.getId().equals(interfaceId.id())).findFirst().orElseThrow())
                .toList();
    }

    public List<PCMInterface> getRequired() {
        return new ArrayList<>(required);
    }

    public List<PCMInterface> getProvided() {
        return new ArrayList<>(provided);
    }

    @XMLClass
    static final class InterfaceId {
        @XMLValue(name = "providedInterface__OperationProvidedRole", mandatory = false)
        private String provided;
        @XMLValue(name = "requiredInterface__OperationRequiredRole", mandatory = false)
        private String required;

        public String id() {
            if (provided == null && required == null)
                throw new IllegalStateException("Required And Provided cannot be null at the same time");
            return provided == null ? required : provided;
        }
    }
}
