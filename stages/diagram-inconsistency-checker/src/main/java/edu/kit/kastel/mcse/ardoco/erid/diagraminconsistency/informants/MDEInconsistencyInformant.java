/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MDEInconsistency;

/**
 * This informant is responsible for finding the {@link MDEInconsistency MDEInconsistencies}
 */
public class MDEInconsistencyInformant extends Informant {
    /**
     * Creates a new informant that acts on the specified data repository
     *
     * @param dataRepository the data repository
     */
    public MDEInconsistencyInformant(DataRepository dataRepository) {
        super(MDEInconsistencyInformant.class.getSimpleName(), dataRepository);
    }

    /**
     * Creates an {@link MDEInconsistency} for each {@link edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendedInstance RecommendedInstance}
     * that is not the endpoint of a {@link edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.LinkBetweenDeAndRi LinkBetweenDeAndRi}.
     */
    @Override
    public void process() {
        var dataRepository = getDataRepository();
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        var diagramInconsistencyStates = dataRepository.getData(DiagramInconsistencyStates.ID, DiagramInconsistencyStates.class).orElseThrow();
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        for (var mm : Metamodel.values()) {
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);
            var diagramInconsistencyState = diagramInconsistencyStates.getDiagramInconsistencyState(mm);
            var allRecommendedInstances = recommendationStates.getRecommendationState(mm).getRecommendedInstances();
            var uncoveredRecommendedInstances = allRecommendedInstances.stream()
                    .filter(ri -> diagramConnectionState.getLinksBetweenDeAndRi(ri).isEmpty())
                    .distinct()
                    .toList();

            uncoveredRecommendedInstances.forEach(ri -> diagramInconsistencyState.addInconsistency(new MDEInconsistency(ri)));
        }
    }
}
