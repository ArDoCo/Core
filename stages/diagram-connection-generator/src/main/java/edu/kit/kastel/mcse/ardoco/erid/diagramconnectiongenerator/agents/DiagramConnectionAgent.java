/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants.DiagramAsModelInformant;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants.DiagramTextInformant;
import edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants.LinkBetweenDeAndRiProbabilityFilter;

/**
 * This agent is responsible for creating and filtering the {@link edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.LinkBetweenDeAndRi} trace links.
 */
public class DiagramConnectionAgent extends PipelineAgent {
    /**
     * Sole constructor of the agent. Runs {@link DiagramAsModelInformant}, {@link DiagramTextInformant}, and {@link LinkBetweenDeAndRiProbabilityFilter} in
     * that order.
     *
     * @param dataRepository the data repository that should be used
     */
    public DiagramConnectionAgent(DataRepository dataRepository) {
        super(List.of(new DiagramAsModelInformant(dataRepository), new DiagramTextInformant(dataRepository), new LinkBetweenDeAndRiProbabilityFilter(
                dataRepository)), DiagramConnectionAgent.class.getSimpleName(), dataRepository);
    }
}
