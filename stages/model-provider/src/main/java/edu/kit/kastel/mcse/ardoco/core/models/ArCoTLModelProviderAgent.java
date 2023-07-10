/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItemRepository;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.Extractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.ArchitectureExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm.PcmExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.uml.UmlExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.AllLanguagesExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code.CodeExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.informants.ArCoTLModelProviderInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * Agent that provides information from models.
 */
public class ArCoTLModelProviderAgent extends PipelineAgent {

    private final List<Informant> informants;

    /**
     * Instantiates a new model provider agent.
     * The constructor takes a list of ModelConnectors that are executed and used to extract information from models.
     *
     * @param data       the DataRepository
     * @param extractors the list of ModelConnectors that should be used
     */
    public ArCoTLModelProviderAgent(DataRepository data, List<Extractor> extractors) {
        super(ArCoTLModelProviderAgent.class.getSimpleName(), data);
        informants = new ArrayList<>();
        for (var extractor : extractors) {
            informants.add(new ArCoTLModelProviderInformant(data, extractor));
        }
    }

    public static ArCoTLModelProviderAgent get(File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCode,
            Map<String, String> additionalConfigs, DataRepository dataRepository) {
        ArchitectureExtractor architectureExtractor = switch (architectureModelType) {
        case PCM -> new PcmExtractor(inputArchitectureModel.getAbsolutePath());
        case UML -> new UmlExtractor(inputArchitectureModel.getAbsolutePath());
        };
        CodeItemRepository codeItemRepository = new CodeItemRepository();
        CodeExtractor codeExtractor = new AllLanguagesExtractor(codeItemRepository, inputCode.getAbsolutePath());
        ArCoTLModelProviderAgent agent = new ArCoTLModelProviderAgent(dataRepository, List.of(architectureExtractor, codeExtractor));
        agent.applyConfiguration(additionalConfigs);
        return agent;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // empty
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return new ArrayList<>(informants);
    }
}
