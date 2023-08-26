package edu.kit.kastel.mcse.ardoco.lissa

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramData
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents.DiagramCleanUpAgent
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents.DiagramDisambiguationAgent
import edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.agents.DiagramReferenceAgent
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.agents.DiagramRecognitionAgent
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.model.DiagramImpl
import java.util.SortedMap

class DiagramRecognition(
    private val diagramRecognitionState: DiagramRecognitionStateImpl,
    dataRepository: DataRepository
) : AbstractExecutionStage(
    listOf(
        DiagramRecognitionAgent(diagramRecognitionState, dataRepository),
        DiagramCleanUpAgent(dataRepository),
        DiagramDisambiguationAgent(dataRepository),
        DiagramReferenceAgent(dataRepository)
    ),
    ID,
    dataRepository
) {

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
        ): DiagramRecognition {
            val diagramDetection = DiagramRecognition(
                DiagramRecognitionStateImpl(),
                dataRepository!!
            )
            diagramDetection.applyConfiguration(additionalConfigs)
            return diagramDetection
        }
    }

    override fun initializeState() {
        dataRepository.addData(DiagramRecognitionState.ID, diagramRecognitionState)

        val inputDiagrams =
            dataRepository.getData(InputDiagramData.ID, InputDiagramData::class.java)
        if (inputDiagrams.isEmpty) {
            return
        }
        logger.info("Initializing DiagramRecognition State")
        for (diagramDatum in inputDiagrams.get().diagramData) {
            val diagram = DiagramImpl(diagramDatum.first, diagramDatum.second)
            diagramRecognitionState.addUnprocessedDiagram(diagram)
        }
    }
}
