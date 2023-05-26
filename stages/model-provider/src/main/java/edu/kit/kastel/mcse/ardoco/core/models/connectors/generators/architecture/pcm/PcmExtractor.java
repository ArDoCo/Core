/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureModel;
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

    public PcmExtractor(String path) {
        super(path);
    }

    /**
     * Extracts an architecture model, i.e. an AMTL instance, from a PCM instance.
     *
     * @return the extracted architecture model
     */
    @Override
    public ArchitectureModel extractModel() {
        PcmModel originalModel = new PcmModel(new File(path));
        List<ArchitectureInterface> interfaces = extractInterfaces(originalModel);
        List<ArchitectureComponent> components = extractComponents(originalModel, interfaces);
        List<ArchitectureItem> endpoints = new ArrayList<>();
        endpoints.addAll(interfaces);
        endpoints.addAll(components);
        return new ArchitectureModel(endpoints);
    }

    @Override
    public ModelType getModelType() {
        return ArchitectureModelType.PCM;
    }

    private static List<ArchitectureInterface> extractInterfaces(PcmModel originalModel) {
        List<ArchitectureInterface> interfaces = new ArrayList<>();
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

    private static List<ArchitectureComponent> extractComponents(PcmModel originalModel, List<ArchitectureInterface> interfaces) {
        List<ArchitectureComponent> components = new ArrayList<>();
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

    private static ArchitectureInterface findInterface(String id, List<ArchitectureInterface> interfaces) {
        return interfaces.stream().filter(modelInterface -> modelInterface.getId().equals(id)).findFirst().orElseThrow();
    }

}
