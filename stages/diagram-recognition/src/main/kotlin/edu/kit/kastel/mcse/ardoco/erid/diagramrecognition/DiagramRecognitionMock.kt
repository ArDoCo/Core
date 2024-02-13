package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.UnicodeCharacterMatchFunctions
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.ExecutionStage
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents.DiagramDisambiguationAgent
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents.DiagramReferenceAgent
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognitionStateImpl
import edu.kit.kastel.mcse.ardoco.tests.eval.GoldStandardDiagrams
import java.util.SortedMap

/**
 * This stage is responsible for mocking the [edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition] stage. It populates the [DiagramRecognitionState]
 * using the [GoldStandardDiagrams] gold standard.
 */
class DiagramRecognitionMock(
    private val goldStandardProject: GoldStandardDiagrams?,
    additionalConfigs: SortedMap<String, String>?,
    dataRepository: DataRepository?
) :
    ExecutionStage(
            listOf(DiagramDisambiguationAgent(dataRepository), DiagramReferenceAgent(dataRepository)),
            DiagramRecognitionMock::class.java.getSimpleName(),
            dataRepository,
            additionalConfigs
        ) {
    private val wordSimUtils: WordSimUtils = getDataRepository().globalConfiguration.wordSimUtils

    override fun initializeState() {
        logger.info("Creating DiagramRecognitionMock State")
        val diagramRecognitionState = DiagramRecognitionStateImpl()
        val diagrams = goldStandardProject?.getDiagramsGoldStandard() ?: listOf()
        for (diagram in diagrams) {
            logger.debug("Loaded Diagram {}", diagram.resourceName)
            diagramRecognitionState.addDiagram(diagram)
        }
        getDataRepository().addData(DiagramRecognitionState.ID, diagramRecognitionState)
    }

    private var previousCharacterMatchFunction: UnicodeCharacterMatchFunctions? = null

    override fun before() {
        super.before()
        previousCharacterMatchFunction = wordSimUtils.characterMatchFunction
        wordSimUtils.characterMatchFunction = UnicodeCharacterMatchFunctions.EQUAL_OR_HOMOGLYPH
    }

    override fun after() {
        wordSimUtils.characterMatchFunction = previousCharacterMatchFunction
        super.after()
    }
}
