package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition;

import java.util.List;
import java.util.SortedMap;
import java.util.function.BiFunction;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacter;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.ExecutionStage;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents.DiagramDisambiguationAgent;
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents.DiagramReferenceAgent;
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognitionStateImpl;
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagrams;

/**
 * This stage is responsible for mocking the {@link edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition} stage. It populates the {@link DiagramRecognitionState}
 * using the {@link GoldStandardDiagrams} gold standard.
 */
public class DiagramRecognitionMock extends ExecutionStage {
    private final GoldStandardDiagrams goldStandardProject;

    public DiagramRecognitionMock(GoldStandardDiagrams goldStandardProject, SortedMap<String, String> additionalConfigs, DataRepository dataRepository) {
        super(List.of(new DiagramDisambiguationAgent(dataRepository), new DiagramReferenceAgent(dataRepository)), "DiagramRecognitionMock", dataRepository,
                additionalConfigs);
        this.goldStandardProject = goldStandardProject;
    }

    @Override
    protected void initializeState() {
        logger.info("Creating DiagramRecognitionMock State");
        var diagramRecognitionState = new DiagramRecognitionStateImpl();
        var diagrams = goldStandardProject.getDiagramsGoldStandard();
        for (var diagram : diagrams) {
            logger.debug("Loaded Diagram {}", diagram.getResourceName());
            diagramRecognitionState.addDiagram(diagram);
        }
        getDataRepository().addData(DiagramRecognitionState.ID, diagramRecognitionState);
    }

    private BiFunction<UnicodeCharacter, UnicodeCharacter, Boolean> previousCharacterMatchFunction;

    @Override
    protected void before() {
        super.before();
        previousCharacterMatchFunction = WordSimUtils.getCharacterMatchFunction();
        WordSimUtils.setCharacterMatchFunction(UnicodeCharacter.EQUAL_OR_HOMOGLYPH);
    }

    @Override
    protected void after() {
        WordSimUtils.setCharacterMatchFunction(previousCharacterMatchFunction);
        super.after();
    }
}
