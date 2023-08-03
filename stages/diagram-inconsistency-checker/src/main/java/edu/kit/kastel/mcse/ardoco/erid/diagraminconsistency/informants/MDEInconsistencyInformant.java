package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramLink;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MDEInconsistency;

public class MDEInconsistencyInformant extends Informant {
    public MDEInconsistencyInformant(DataRepository dataRepository) {
        super(MDEInconsistencyInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        var diagramInconsistencyStates = dataRepository.getData(DiagramInconsistencyStates.ID, DiagramInconsistencyStates.class).orElseThrow();
        var modelIds = modelStates.extractionModelIds();
        for (var model : modelIds) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel mm = modelState.getMetamodel();
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);
            var diagramInconsistencyState = diagramInconsistencyStates.getDiagramInconsistencyState(mm);
            var allRecommendedInstances = diagramInconsistencyState.getRecommendedInstances();
            var coveredRecommendedInstances = diagramConnectionState.getDiagramLinks().stream().map(DiagramLink::getRecommendedInstance).distinct().toList();
            var uncoveredRecommendedInstances = allRecommendedInstances.stream().filter(b -> !coveredRecommendedInstances.contains(b)).distinct().toList();

            uncoveredRecommendedInstances.forEach(ri -> diagramInconsistencyState.addInconsistency(new MDEInconsistency(ri)));
        }
    }
}
