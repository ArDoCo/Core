package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.agents;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants.InconsistencyByRecommendedInstancesInformant;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants.InconsistencyByDiagramElementsInformant;

public class InconsistencyAgent extends PipelineAgent {
    @Configurable
    private List<String> enabledInformants;

    public InconsistencyAgent(DataRepository dataRepository) {
        super(InconsistencyAgent.class.getSimpleName(), dataRepository,
                List.of(new InconsistencyByRecommendedInstancesInformant(dataRepository), new InconsistencyByDiagramElementsInformant(dataRepository)));
        enabledInformants = getInformantClassNames();
    }

    @Override
    protected List<Informant> getEnabledPipelineSteps() {
        return findByClassName(enabledInformants, getInformants());
    }
}
