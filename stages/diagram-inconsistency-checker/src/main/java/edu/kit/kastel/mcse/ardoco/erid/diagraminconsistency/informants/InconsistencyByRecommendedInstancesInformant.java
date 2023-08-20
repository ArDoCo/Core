package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants;

import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MDEInconsistency;

public class InconsistencyByRecommendedInstancesInformant extends Informant {
    public InconsistencyByRecommendedInstancesInformant(DataRepository dataRepository) {
        super(InconsistencyByRecommendedInstancesInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        var diagramInconsistencyStates = dataRepository.getData(DiagramInconsistencyStates.ID, DiagramInconsistencyStates.class).orElseThrow();
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var modelIds = modelStates.extractionModelIds();
        for (var model : modelIds) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel mm = modelState.getMetamodel();
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
