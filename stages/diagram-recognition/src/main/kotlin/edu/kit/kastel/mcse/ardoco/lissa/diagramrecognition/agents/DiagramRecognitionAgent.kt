package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.agents

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.common.util.SerializableFileBasedCache
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognitionStateImpl
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants.ObjectDetectionInformant
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants.OcrInformant
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants.RecognitionCombinatorInformant
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.model.SketchRecognitionResult

/**
 * This agent uses the [DiagramRecognitionState] to extract the diagrams and sketches from images.
 */
class DiagramRecognitionAgent(
    val diagramRecognitionState:
    DiagramRecognitionStateImpl, dataRepository: DataRepository
) : PipelineAgent(
    ID,
    dataRepository,
    listOf(
        ObjectDetectionInformant(diagramRecognitionState, dataRepository),
        OcrInformant(diagramRecognitionState, dataRepository),
        RecognitionCombinatorInformant(diagramRecognitionState, dataRepository)
    )
) {
    companion object {
        const val ID = "DiagramRecognitionAgent"
    }

    @Configurable
    private var enabledInformants: MutableList<String> =
        informants.map { e: Informant -> e.javaClass.simpleName }.toMutableList()

    override fun getEnabledPipelineSteps(): MutableList<Informant> =
        findByClassName(enabledInformants, informants)

    override fun before() {
        for (diagram in diagramRecognitionState.getUnprocessedDiagrams()) {
            val cached = SerializableFileBasedCache(
                SketchRecognitionResult::class
                    .java, diagram.resourceName, "diagram-recognition/"
            ).getOrRead()
            if (cached == null) {
                logger.info("${diagram.resourceName} is not cached")
            } else {
                cached.boxes.forEach(diagram::addBox)
                cached.textBoxes.forEach(diagram::addTextBox)
                cached.edges.forEach(diagram::addConnector)
                logger.info("${diagram.resourceName} loaded from cache")
                diagramRecognitionState.addDiagram(diagram)
                diagramRecognitionState.removeUnprocessedDiagram(diagram)
            }
        }
    }

    override fun after() {
        for (diagram in diagramRecognitionState.getUnprocessedDiagrams()) {
            diagramRecognitionState.addDiagram(diagram)

            logger.info("Caching {}", diagram.resourceName)
            SerializableFileBasedCache(
                SketchRecognitionResult::class
                    .java, diagram.resourceName, "diagram-recognition/"
            ).use {
                it.cache(
                    SketchRecognitionResult(
                        diagram.boxes,
                        diagram.textBoxes,
                        diagram.connectors
                    )
                )
            }
        }
    }
}
