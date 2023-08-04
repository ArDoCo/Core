package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramLink;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MTDEInconsistency;

public class MTDEInconsistencyInformant extends Informant {
    public MTDEInconsistencyInformant(DataRepository dataRepository) {
        super(MTDEInconsistencyInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var diagramRecognitionState = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).orElseThrow();
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        var diagramInconsistencyStates = dataRepository.getData(DiagramInconsistencyStates.ID, DiagramInconsistencyStates.class).orElseThrow();
        var modelIds = modelStates.extractionModelIds();
        for (var model : modelIds) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel mm = modelState.getMetamodel();
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);
            var diagramInconsistencyState = diagramInconsistencyStates.getDiagramInconsistencyState(mm);
            var allDiagramElements = diagramRecognitionState.getDiagrams().stream().flatMap(d -> d.getBoxes().stream()).distinct().toList();
            var coveredDiagramElements = diagramConnectionState.getDiagramLinks().stream().map(DiagramLink::getDiagramElement).distinct().toList();
            var uncoveredDiagramElements = allDiagramElements.stream().filter(b -> !coveredDiagramElements.contains(b)).distinct().toList();
            assert coveredDiagramElements.size() + uncoveredDiagramElements.size() == allDiagramElements.size();

            uncoveredDiagramElements.forEach(de -> diagramInconsistencyState.addInconsistency(new MTDEInconsistency(de)));
        }
    }
}
