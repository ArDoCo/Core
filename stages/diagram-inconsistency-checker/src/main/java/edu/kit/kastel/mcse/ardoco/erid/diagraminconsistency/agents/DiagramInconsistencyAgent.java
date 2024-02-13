/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants.MDEInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants.MTDEInconsistencyInformant;

/**
 * This agent is responsible for the creation of the diagram element {@link edu.kit.kastel.mcse.ardoco.core.api.inconsistency.Inconsistency Inconsistencies}
 * such as the {@link edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MDEInconsistency MDEInconsistency} and
 * {@link edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MTDEInconsistency MTDEInconsistency}.
 */
public class DiagramInconsistencyAgent extends PipelineAgent {

    public DiagramInconsistencyAgent(DataRepository dataRepository) {
        super(List.of(new MDEInconsistencyInformant(dataRepository), new MTDEInconsistencyInformant(dataRepository)), DiagramInconsistencyAgent.class
                .getSimpleName(), dataRepository);
    }
}
