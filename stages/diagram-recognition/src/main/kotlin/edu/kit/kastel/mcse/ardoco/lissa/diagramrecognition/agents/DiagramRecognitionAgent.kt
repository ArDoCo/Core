package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.agents

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants.ObjectDetectionInformant
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants.OcrInformant
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants.RecognitionCombinatorInformant
import java.util.SortedMap

/**
 * This agent uses the [DiagramRecognitionState] to extract the diagrams and sketches from images.
 */
class DiagramRecognitionAgent(dataRepository: DataRepository) : PipelineAgent(ID, dataRepository) {
    companion object {
        const val ID = "DiagramRecognitionAgent"
    }

    private val informants = listOf(
        ObjectDetectionInformant(dataRepository),
        OcrInformant(dataRepository),
        RecognitionCombinatorInformant(dataRepository)
    )

    @Configurable
    private var enabledInformants = informants.map { e: Informant -> e.id }.toMutableList()

    override fun getEnabledPipelineStepIds(): List<String> = enabledInformants

    override fun getAllPipelineSteps(): List<Informant> = informants
    override fun delegateApplyConfigurationToInternalObjects(additionalConfiguration: SortedMap<String?, String?>?) {
        informants.forEach { it.applyConfiguration(additionalConfiguration) }
    }
}
