/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.AggregationFunctions;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionState;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;

/**
 * This informant filters the diagram elements and recommended instances that are below a configurable confidence threshold.
 */
public class LinkBetweenDeAndRiProbabilityFilter extends Informant {
    @Configurable
    private double confidenceThreshold = 0.75;

    public LinkBetweenDeAndRiProbabilityFilter(DataRepository dataRepository) {
        super(LinkBetweenDeAndRiProbabilityFilter.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        var dataRepository = getDataRepository();
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        for (var mm : Metamodel.values()) {
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);

            filterByProbability(diagramConnectionState);
        }
    }

    /**
     * Calculates the overall confidence in each trace link using {@link AggregationFunctions#MAX} and removes all below a certain threshold.
     *
     * @param diagramConnectionState the diagram connection state
     */
    private void filterByProbability(DiagramConnectionState diagramConnectionState) {
        var belowThreshold = diagramConnectionState.getLinksBetweenDeAndRi()
                .stream()
                .filter(linkBetweenDeAndRi -> linkBetweenDeAndRi.getConfidence(AggregationFunctions.MAX) < confidenceThreshold)
                .toList();
        logger.info("Removed {} Diagram Links due to low confidence", belowThreshold.size());
        belowThreshold.forEach(b -> logger.info(b.toString()));
        belowThreshold.forEach(diagramConnectionState::removeFromLinksBetweenDeAndRi);
    }
}
