/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.agents;

import java.io.File;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants.DiagramProviderInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * Agent that provides a diagram.
 */
public class DiagramProviderAgent extends PipelineAgent {
    /**
     * Creates a new DiagramProviderAgent.
     *
     * @param data
     *                    The DataRepository.
     * @param diagramFile
     *                    The diagram file.
     */
    public DiagramProviderAgent(DataRepository data, File diagramFile) {
        super(List.of(new DiagramProviderInformant(data, diagramFile)), DiagramProviderAgent.class.getSimpleName(), data);
    }

    /**
     * Creates a new DiagramProviderAgent that will load the diagram from the given file.
     *
     * @param diagramFile
     *                       The diagram file.
     * @param dataRepository
     *                       The DataRepository.
     * @return The DiagramProviderAgent.
     */
    public static DiagramProviderAgent get(File diagramFile, DataRepository dataRepository) {
        return new DiagramProviderAgent(dataRepository, diagramFile);
    }
}
