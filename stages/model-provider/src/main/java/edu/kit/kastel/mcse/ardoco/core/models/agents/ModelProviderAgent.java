/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.agents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.configuration.NoConfiguration;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.PcmXmlModelConnector;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.UmlModelConnector;
import edu.kit.kastel.mcse.ardoco.core.models.informants.LegacyCodeModelInformant;
import edu.kit.kastel.mcse.ardoco.core.models.informants.ModelDisambiguationInformant;
import edu.kit.kastel.mcse.ardoco.core.models.informants.ModelProviderInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * Agent that provides information from models.
 */
@NoConfiguration
public class ModelProviderAgent extends PipelineAgent {

    /**
     * Instantiates a new model provider agent. The constructor takes a list of ModelConnectors that are executed and used to extract information from models.
     *
     * @param data            the DataRepository
     * @param modelConnectors the list of ModelConnectors that should be used
     */
    public ModelProviderAgent(DataRepository data, List<ModelConnector> modelConnectors) {
        super(createInformants(modelConnectors, data), ModelProviderAgent.class.getSimpleName(), data);
    }

    private static List<? extends Informant> createInformants(List<ModelConnector> modelConnectors, DataRepository data) {
        ArrayList<Informant> list = modelConnectors.stream().map(e -> new ModelProviderInformant(data, e)).collect(Collectors.toCollection(ArrayList::new));
        list.add(new ModelDisambiguationInformant(data));
        return list;
    }

    private ModelProviderAgent(DataRepository data, LegacyCodeModelInformant codeModelInformant) {
        super(List.of(codeModelInformant), ModelProviderAgent.class.getSimpleName(), data);
    }

    /**
     * Creates a {@link ModelProviderInformant} for PCM.
     *
     * @param inputArchitectureModel the path to the input PCM
     * @param architectureModelType  the architecture model to use
     * @param dataRepository         the data repository
     * @return A ModelProvider for the PCM
     * @throws IOException if the Code Model cannot be accessed
     */
    public static ModelProviderAgent get(File inputArchitectureModel, ArchitectureModelType architectureModelType, DataRepository dataRepository)
            throws IOException {
        ModelConnector connector = switch (architectureModelType) {
        case PCM -> new PcmXmlModelConnector(inputArchitectureModel);
        case UML -> new UmlModelConnector(inputArchitectureModel);
        };
        return new ModelProviderAgent(dataRepository, List.of(connector));
    }

    public static ModelProviderAgent getCodeProvider(DataRepository dataRepository) {
        return new ModelProviderAgent(dataRepository, new LegacyCodeModelInformant(dataRepository));
    }
}
