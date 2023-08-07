package edu.kit.kastel.mcse.ardoco.erid.diagramconnectiongenerator.informants;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionState;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.DiagramLink;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiagramLinkProbabilityFilter extends Informant {
    private final static Logger logger = LoggerFactory.getLogger(DiagramLinkProbabilityFilter.class);

    @Configurable
    private double confidenceThreshold = 0.9;

    public DiagramLinkProbabilityFilter(DataRepository dataRepository) {
        super(DiagramLinkProbabilityFilter.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        var modelIds = modelStates.extractionModelIds();
        for (var model : modelIds) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel mm = modelState.getMetamodel();
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);

            filterByProbability(diagramConnectionState);
        }
    }

    private void filterByProbability(DiagramConnectionState diagramConnectionState) {
        var belowThreshold = diagramConnectionState.getDiagramLinks()
                .stream()
                .filter(diagramLink -> diagramLink.getConfidence(DiagramLink.MAXIMUM_CONFIDENCE) < confidenceThreshold).toList();
        logger.info("Removed {} Diagram Links due to low confidence", belowThreshold.size());
        belowThreshold.forEach(diagramConnectionState::removeFromDiagramLinks);
    }
}
