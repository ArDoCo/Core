package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants.InconsistencyCheckerFiltersInformant;

public class RecommendedInstancesFilterAgent extends PipelineAgent {
    @Configurable
    private List<String> enabledInformants;

    public RecommendedInstancesFilterAgent(DataRepository dataRepository) {
        super(RecommendedInstancesFilterAgent.class.getSimpleName(), dataRepository, List.of(new InconsistencyCheckerFiltersInformant(dataRepository)));
        enabledInformants = getInformantClassNames();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
