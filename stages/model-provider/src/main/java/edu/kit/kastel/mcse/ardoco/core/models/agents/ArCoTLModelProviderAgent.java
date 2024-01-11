/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.models.agents;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

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

    /**
     * Instantiates a new model provider agent.
     * The constructor takes a list of ModelConnectors that are executed and used to extract information from models.
     *
     * @param data       the DataRepository
     * @param extractors the list of ModelConnectors that should be used
     */
    public ArCoTLModelProviderAgent(DataRepository data, List<Extractor> extractors) {
        super(informants(data, extractors), ArCoTLModelProviderAgent.class.getSimpleName(), data);
    }

    private static List<? extends Informant> informants(DataRepository data, List<Extractor> extractors) {
        return extractors.stream().map(e -> new ArCoTLModelProviderInformant(data, e)).toList();
    }

    public static ArCoTLModelProviderAgent get(File inputArchitectureModel, ArchitectureModelType architectureModelType, File inputCode,
            SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {

        List<Extractor> extractors = new ArrayList<>();

        if (inputArchitectureModel != null && architectureModelType != null) {
            ArchitectureExtractor architectureExtractor = switch (architectureModelType) {
            case PCM -> new PcmExtractor(inputArchitectureModel.getAbsolutePath());
            case UML -> new UmlExtractor(inputArchitectureModel.getAbsolutePath());
            };
            extractors.add(architectureExtractor);
        }

        if (inputCode != null) {
            CodeItemRepository codeItemRepository = new CodeItemRepository();
            CodeExtractor codeExtractor = new AllLanguagesExtractor(codeItemRepository, inputCode.getAbsolutePath());
            extractors.add(codeExtractor);
        }

        if (extractors.isEmpty()) {
            throw new IllegalArgumentException("No model extractor was provided.");
        }

        ArCoTLModelProviderAgent agent = new ArCoTLModelProviderAgent(dataRepository, extractors);
        agent.applyConfiguration(additionalConfigs);
        return agent;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // empty
    }
}
