/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.agents;

import java.util.List;
import java.util.Optional;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramModelInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramInconsistencyStateImpl;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants.DiagramModelInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants.InconsistencyGroupingInformant;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants.InconsistencyRefinementInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * This agent is responsible for finding inconsistencies between the diagram and the given models.
 */
public class DiagramModelInconsistencyAgent extends PipelineAgent {
    /**
     * Creates a new DiagramModelInconsistencyAgent.
     *
     * @param data
     *             The DataRepository.
     */
    public DiagramModelInconsistencyAgent(DataRepository data) {
        super(List.of(new DiagramModelInconsistencyInformant(data), new InconsistencyRefinementInformant(data), new InconsistencyGroupingInformant(data)),
                DiagramModelInconsistencyAgent.class.getSimpleName(), data);
    }

    /**
     * Creates a new DiagramModelInconsistencyAgent.
     *
     * @param dataRepository
     *                       The DataRepository.
     * @return The DiagramModelInconsistencyAgent.
     */
    public static DiagramModelInconsistencyAgent get(DataRepository dataRepository) {
        return new DiagramModelInconsistencyAgent(dataRepository);
    }

    @Override
    protected void initializeState() {
        DataRepository data = this.getDataRepository();
        Optional<DiagramModelInconsistencyState> optionalDiagramModelInconsistencyState = data.getData(DiagramModelInconsistencyState.ID,
                DiagramModelInconsistencyState.class);
        DiagramModelInconsistencyState state = optionalDiagramModelInconsistencyState.orElseGet(DiagramInconsistencyStateImpl::new);
        if (optionalDiagramModelInconsistencyState.isEmpty()) {
            data.addData(DiagramModelInconsistencyState.ID, state);
        }
    }
}
