/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.uml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.ArchitectureExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.uml.parser.UmlComponent;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.uml.parser.UmlInterface;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.uml.parser.UmlModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.uml.parser.xmlelements.OwnedOperation;

/**
 * An extractor for UML. Extracts an AMTL instance.
 */
public final class UmlExtractor extends ArchitectureExtractor {

    private static final UmlExtractor extractor = new UmlExtractor();

    private UmlExtractor() {
    }

    public static UmlExtractor getExtractor() {
        return extractor;
    }

    /**
     * Extracts an architecture model, i.e. an AMTL instance, from a UML instance.
     *
     * @param path the path of the UML instance's repository
     * @return the extracted architecture model
     */
    @Override
    public ArchitectureModel extractModel(String path) {
        UmlModel originalModel = new UmlModel(new File(path));
        Set<ArchitectureInterface> interfaces = extractInterfaces(originalModel);
        Set<ArchitectureComponent> components = extractComponents(originalModel, interfaces);
        Set<ArchitectureItem> endpoints = new HashSet<>();
        endpoints.addAll(interfaces);
        endpoints.addAll(components);
        ArchitectureModel model = new ArchitectureModel(endpoints);
        return model;
    }

    private static Set<ArchitectureInterface> extractInterfaces(UmlModel originalModel) {
        Set<ArchitectureInterface> interfaces = new HashSet<>();
        for (UmlInterface originalInterface : originalModel.getModel().getInterfaces()) {
            Set<ArchitectureMethod> signatures = new HashSet<>();
            for (OwnedOperation originalMethod : originalInterface.getOperations()) {
                ArchitectureMethod signature = new ArchitectureMethod(originalMethod.getName());
                signatures.add(signature);
            }
            ArchitectureInterface modelInterface = new ArchitectureInterface(originalInterface.getName(), originalInterface.getId(), signatures);
            interfaces.add(modelInterface);
        }
        return interfaces;
    }

    private static Set<ArchitectureComponent> extractComponents(UmlModel originalModel, Set<ArchitectureInterface> interfaces) {
        Set<ArchitectureComponent> components = new HashSet<>();
        for (UmlComponent originalComponent : originalModel.getModel().getComponents()) {
            Set<ArchitectureComponent> subcomponents = new HashSet<>();
            Set<ArchitectureInterface> providedInterfaces = new HashSet<>();
            Set<ArchitectureInterface> requiredInterfaces = new HashSet<>();
            for (UmlInterface providedInterface : originalComponent.getProvided()) {
                ArchitectureInterface modelInterface = findInterface(providedInterface.getId(), interfaces);
                providedInterfaces.add(modelInterface);
            }
            for (UmlInterface requiredInterface : originalComponent.getRequired()) {
                ArchitectureInterface modelInterface = findInterface(requiredInterface.getId(), interfaces);
                requiredInterfaces.add(modelInterface);
            }
            ArchitectureComponent modelComponent = new ArchitectureComponent(originalComponent.getName(), originalComponent.getId(), subcomponents,
                    providedInterfaces, requiredInterfaces);
            components.add(modelComponent);
        }
        return components;
    }

    private static ArchitectureInterface findInterface(String id, Set<ArchitectureInterface> interfaces) {
        return interfaces.stream().filter(modelInterface -> modelInterface.getId().equals(id)).findFirst().orElseThrow();
    }
}
