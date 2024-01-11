/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants.DiagramDisambiguationInformant;

/**
 * Responsible for disambiguating abbreviations in {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement DiagramElements}.
 *
 * @see DiagramDisambiguationInformant
 */
public class DiagramDisambiguationAgent extends PipelineAgent {
    public DiagramDisambiguationAgent(DataRepository dataRepository) {
        super(List.of(new DiagramDisambiguationInformant(dataRepository)), DiagramDisambiguationAgent.class.getSimpleName(), dataRepository);
    }
}
