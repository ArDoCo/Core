package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants.InconsistencyByDiagramElementsInformant;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants.InconsistencyByRecommendedInstancesInformant;

public class InconsistencyAgent extends PipelineAgent {

    public InconsistencyAgent(DataRepository dataRepository) {
        super(List.of(new InconsistencyByRecommendedInstancesInformant(dataRepository), new InconsistencyByDiagramElementsInformant(dataRepository)),
                InconsistencyAgent.class.getSimpleName(), dataRepository);
    }
}
