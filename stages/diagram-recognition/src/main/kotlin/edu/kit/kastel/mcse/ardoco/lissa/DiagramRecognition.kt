package edu.kit.kastel.mcse.ardoco.lissa

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramData
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.agents.DiagramRecognitionAgent
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.model.DiagramImpl
import java.util.SortedMap

class DiagramRecognition(dataRepository: DataRepository) : AbstractExecutionStage(listOf(DiagramRecognitionAgent(dataRepository)), ID, dataRepository) {
    companion object {
        const val ID = "DiagramRecognition"

        /**
         * Creates a [DiagramRecognition] that will handle the diagram recognition.
         *
         * @param additionalConfigs the additional configuration that should be applied
         * @param dataRepository    the data repository
         * @return a DiagramRecognition with the provided diagrams
         */
        @JvmStatic
        fun get(
            additionalConfigs: SortedMap<String?, String?>?,
            dataRepository: DataRepository?
        ): DiagramRecognition? {
            val diagramDetection = DiagramRecognition(dataRepository!!)
            diagramDetection.applyConfiguration(additionalConfigs)
            return diagramDetection
        }
    }

    override fun initializeState() {
        val inputDiagrams = dataRepository.getData(InputDiagramData.ID, InputDiagramData::class.java)
        if (inputDiagrams.isEmpty) {
            return
        }
        logger.info("Creating DiagramRecognition State")
        val diagramRecognitionState = DiagramRecognitionStateImpl()
        for (diagramFile in inputDiagrams.get().files) {
            val diagram = DiagramImpl(diagramFile)
            logger.debug("Loaded Diagram {}", diagramFile)
            diagramRecognitionState.addDiagram(diagram)
        }
        dataRepository.addData(DiagramRecognitionState.ID, diagramRecognitionState)
    }
}
