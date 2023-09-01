package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants.InfluenceByInconsistenciesInformant;

public class RecommendedInstancesConfidenceAgent extends PipelineAgent {

    public RecommendedInstancesConfidenceAgent(DataRepository dataRepository) {
        super(List.of(new InfluenceByInconsistenciesInformant(dataRepository)), RecommendedInstancesConfidenceAgent.class.getSimpleName(), dataRepository);
    }
}