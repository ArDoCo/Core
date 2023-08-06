package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants.InfluenceByInconsistenciesInformant;

public class RecommendedInstancesConfidenceAgent extends PipelineAgent {
    @Configurable
    private List<String> enabledInformants = getInformantClassNames();

    public RecommendedInstancesConfidenceAgent(DataRepository dataRepository) {
        super(RecommendedInstancesConfidenceAgent.class.getSimpleName(), dataRepository, List.of(new InfluenceByInconsistenciesInformant(dataRepository)));
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
