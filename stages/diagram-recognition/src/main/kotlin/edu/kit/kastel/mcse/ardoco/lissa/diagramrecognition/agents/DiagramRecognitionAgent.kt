package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.agents

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper
import edu.kit.kastel.mcse.ardoco.core.common.util.SerializableFileBasedCache
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants.ObjectDetectionInformant
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants.OcrInformant
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants.RecognitionCombinatorInformant
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.model.SketchRecognitionResult
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.visualize
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * This agent uses the [DiagramRecognitionState] to extract the diagrams and sketches from images.
 */
class DiagramRecognitionAgent(
    dataRepository: DataRepository
) : PipelineAgent(
        listOf(
            ObjectDetectionInformant(dataRepository),
            OcrInformant(dataRepository),
            RecognitionCombinatorInformant(dataRepository)
        ),
        ID,
        dataRepository
    ) {
    companion object {
        const val ID = "DiagramRecognitionAgent"
        const val ENV_DEBUG_VISUALIZE = "debugVisualize"
    }

    override fun before() {
        val diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(dataRepository)
        for (diagram in diagramRecognitionState.getUnprocessedDiagrams()) {
            val cached =
                SerializableFileBasedCache(
                    SketchRecognitionResult::class
                        .java,
                    diagram.resourceName,
                    "diagram-recognition/"
                ).getOrRead()
            if (cached == null) {
                logger.info("${diagram.resourceName} is not cached")
            } else {
                cached.boxes.forEach(diagram::addBox)
                cached.textBoxes.forEach(diagram::addTextBox)
                cached.edges.forEach(diagram::addConnector)
                logger.info("${diagram.resourceName} loaded from cache")
                diagramRecognitionState.addDiagram(diagram)

                debugVisualize(diagram)

                diagramRecognitionState.removeUnprocessedDiagram(diagram)
            }
        }
    }

    override fun after() {
        val diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(dataRepository)
        for (diagram in diagramRecognitionState.getUnprocessedDiagrams()) {
            diagramRecognitionState.addDiagram(diagram)

            logger.info("Caching {}", diagram.resourceName)
            SerializableFileBasedCache(
                SketchRecognitionResult::class
                    .java,
                diagram.resourceName,
                "diagram-recognition/"
            ).use {
                it.cache(
                    SketchRecognitionResult(
                        diagram.boxes,
                        diagram.textBoxes,
                        diagram.connectors
                    )
                )
            }

            debugVisualize(diagram)
        }
    }

    private fun debugVisualize(diagram: Diagram) {
        if (System.getenv().getOrDefault(ENV_DEBUG_VISUALIZE, "false").toBoolean()) {
            val destination = File.createTempFile("ArDoCo", ".png")
            visualize(
                FileInputStream(diagram.location),
                diagram,
                FileOutputStream(destination)
            )
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(destination)
        } else {
            logger.info("Set \"$ENV_DEBUG_VISUALIZE=true\" to enable diagram recognition visualization")
        }
    }
}
