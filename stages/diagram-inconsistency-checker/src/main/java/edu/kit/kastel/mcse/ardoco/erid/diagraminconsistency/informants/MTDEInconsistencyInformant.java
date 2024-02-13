/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.informants;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.erid.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.erid.api.diagraminconsistency.DiagramInconsistencyStates;
import edu.kit.kastel.mcse.ardoco.erid.diagraminconsistency.types.MTDEInconsistency;

/**
 * This informant is responsible for finding the {@link MTDEInconsistency MTDEInconsistencies}
 */
public class MTDEInconsistencyInformant extends Informant {
    /**
     * Creates a new informant that acts on the specified data repository
     *
     * @param dataRepository the data repository
     */
    public MTDEInconsistencyInformant(DataRepository dataRepository) {
        super(MTDEInconsistencyInformant.class.getSimpleName(), dataRepository);
    }

    /**
     * Creates an {@link MTDEInconsistency} for each {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement DiagramElement} that is not
     * the endpoint of a {@link edu.kit.kastel.mcse.ardoco.erid.api.models.tracelinks.LinkBetweenDeAndRi LinkBetweenDeAndRi}.
     */
    @Override
    public void process() {
        var dataRepository = getDataRepository();
        var diagramRecognitionState = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).orElseThrow();
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).orElseThrow();
        var diagramInconsistencyStates = dataRepository.getData(DiagramInconsistencyStates.ID, DiagramInconsistencyStates.class).orElseThrow();
        for (var mm : Metamodel.values()) {
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);
            var diagramInconsistencyState = diagramInconsistencyStates.getDiagramInconsistencyState(mm);
            var allDiagramElements = diagramRecognitionState.getDiagrams().stream().flatMap(d -> d.getBoxes().stream()).distinct().toList();
            var uncoveredDiagramElements = allDiagramElements.stream()
                    .filter(de -> diagramConnectionState.getLinksBetweenDeAndRi(de).isEmpty())
                    .distinct()
                    .toList();

            uncoveredDiagramElements.forEach(de -> diagramInconsistencyState.addInconsistency(new MTDEInconsistency(de)));
        }
    }
}
