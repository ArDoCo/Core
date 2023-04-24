package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.ArchitectureExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm.parser.PcmComponent;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm.parser.PcmInterface;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm.parser.PcmModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm.parser.PcmSignature;

// TODO we currently more or less have two connectors/extractors: this one and the PcmXmlModelConnector

/**
 * An extractor for PCM. Extracts an AMTL instance.
 */
public final class PcmExtractor extends ArchitectureExtractor {

    private static final PcmExtractor extractor = new PcmExtractor();

    private PcmExtractor() {
    }

    public static PcmExtractor getExtractor() {
        return extractor;
    }

    /**
     * Extracts an architecture model, i.e. an AMTL instance, from a PCM instance.
     *
     * @param path the path of the PCM instance's repository
     * @return the extracted architecture model
     */
    @Override
    public ArchitectureModel extractModel(String path) {
        PcmModel originalModel = new PcmModel(new File(path));
        Set<ArchitectureInterface> interfaces = extractInterfaces(originalModel);
        Set<ArchitectureComponent> components = extractComponents(originalModel, interfaces);
        Set<ArchitectureItem> endpoints = new HashSet<>();
        endpoints.addAll(interfaces);
        endpoints.addAll(components);
        ArchitectureModel model = new ArchitectureModel(endpoints);
        return model;
    }

    private static Set<ArchitectureInterface> extractInterfaces(PcmModel originalModel) {
        Set<ArchitectureInterface> interfaces = new HashSet<>();
        for (PcmInterface originalInterface : originalModel.getRepository().getInterfaces()) {
            Set<ArchitectureMethod> signatures = new HashSet<>();
            for (PcmSignature originalMethod : originalInterface.getMethods()) {
                ArchitectureMethod signature = new ArchitectureMethod(originalMethod.getEntityName());
                signatures.add(signature);
            }
            ArchitectureInterface modelInterface = new ArchitectureInterface(originalInterface.getEntityName(), originalInterface.getId(), signatures);
            interfaces.add(modelInterface);
        }
        return interfaces;
    }

    private static Set<ArchitectureComponent> extractComponents(PcmModel originalModel, Set<ArchitectureInterface> interfaces) {
        Set<ArchitectureComponent> components = new HashSet<>();
        for (PcmComponent originalComponent : originalModel.getRepository().getComponents()) {
            Set<ArchitectureComponent> subcomponents = new HashSet<>();
            Set<ArchitectureInterface> providedInterfaces = new HashSet<>();
            Set<ArchitectureInterface> requiredInterfaces = new HashSet<>();
            for (PcmInterface providedInterface : originalComponent.getProvided()) {
                ArchitectureInterface modelInterface = findInterface(providedInterface.getId(), interfaces);
                providedInterfaces.add(modelInterface);
            }
            for (PcmInterface requiredInterface : originalComponent.getRequired()) {
                ArchitectureInterface modelInterface = findInterface(requiredInterface.getId(), interfaces);
                requiredInterfaces.add(modelInterface);
            }
            ArchitectureComponent modelComponent = new ArchitectureComponent(originalComponent.getEntityName(), originalComponent.getId(), subcomponents,
                    providedInterfaces, requiredInterfaces);
            components.add(modelComponent);
        }
        return components;
    }

    private static ArchitectureInterface findInterface(String id, Set<ArchitectureInterface> interfaces) {
        return interfaces.stream().filter(modelInterface -> modelInterface.getId().equals(id)).findFirst().orElseThrow();
    }

}
