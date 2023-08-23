package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.agents

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants.ObjectDetectionInformant
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants.OcrInformant
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants.RecognitionCombinatorInformant

/**
 * This agent uses the [DiagramRecognitionState] to extract the diagrams and sketches from images.
 */
class DiagramRecognitionAgent(dataRepository: DataRepository) : PipelineAgent(
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
    }
}
