/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.agents;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramMatchingModelSelectionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramMatchingModelSelectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants.DiagramElementOccurrenceFinderInformant;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants.OccurrenceToDecisionInformant;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants.WeightedSimilarityInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;

/**
 * Selects the model a diagram probably represents.
 */
public class DiagramModelSelectionAgent extends PipelineAgent {
    private final Set<ModelType> availableModelTypes;

    /**
     * Creates a new DiagramModelSelectionAgent.
     *
     * @param availableModelTypes
     *                            All model types that are in principle available, meaning their loading is attempted.
     * @param dataRepository
     *                            The DataRepository.
     */
    public DiagramModelSelectionAgent(Set<ModelType> availableModelTypes, DataRepository dataRepository) {
        super(List.of(new WeightedSimilarityInformant(dataRepository), new DiagramElementOccurrenceFinderInformant(dataRepository),
                new OccurrenceToDecisionInformant(dataRepository)), DiagramModelSelectionAgent.class.getSimpleName(), dataRepository);

        this.availableModelTypes = availableModelTypes;
    }

    /**
     * Creates a new DiagramModelSelectionAgent.
     *
     * @param availableModelTypes
     *                            All model types that are in principle available, meaning their loading is attempted.
     * @param dataRepository
     *                            The DataRepository.
     * @return The DiagramModelSelectionAgent.
     */
    public static DiagramModelSelectionAgent get(Set<ModelType> availableModelTypes, DataRepository dataRepository) {
        return new DiagramModelSelectionAgent(availableModelTypes, dataRepository);
    }

    @Override
    protected void initializeState() {
        DataRepository data = this.getDataRepository();

        Optional<DiagramMatchingModelSelectionState> optionalModelSelectionState = data.getData(DiagramMatchingModelSelectionState.ID,
                DiagramMatchingModelSelectionState.class);

        DiagramMatchingModelSelectionState state = optionalModelSelectionState.orElseGet(DiagramMatchingModelSelectionStateImpl::new);

        state.setAvailableModelTypes(this.availableModelTypes);

        if (optionalModelSelectionState.isEmpty()) {
            data.addData(DiagramMatchingModelSelectionState.ID, state);
        }
    }
}
