package edu.kit.kastel.mcse.ardoco.lissa

import edu.kit.kastel.mcse.ardoco.core.api.InputDiagramData
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.agents.DiagramRecognitionAgent
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.model.DiagramImpl

class DiagramRecognition : AbstractExecutionStage {

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
        fun get(additionalConfigs: Map<String?, String?>?, dataRepository: DataRepository?): DiagramRecognition? {
            val diagramDetection = DiagramRecognition(dataRepository!!)
            diagramDetection.applyConfiguration(additionalConfigs)
            return diagramDetection
        }
    }

    private val agents: List<PipelineAgent>

    @Configurable
    private var enabledAgents: MutableList<String>

    constructor(dataRepository: DataRepository) : super(ID, dataRepository) {
        this.agents = listOf(DiagramRecognitionAgent(dataRepository))
        enabledAgents = this.agents.map { it.id }.toMutableList()
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

    override fun getEnabledAgents(): MutableList<PipelineAgent> {
        return findByClassName(enabledAgents, agents)
    }

    override fun delegateApplyConfigurationToInternalObjects(additionalConfiguration: Map<String?, String?>) {
        super.delegateApplyConfigurationToInternalObjects(additionalConfiguration)
        for (agent in agents) {
            agent.applyConfiguration(additionalConfiguration)
        }
    }
}
