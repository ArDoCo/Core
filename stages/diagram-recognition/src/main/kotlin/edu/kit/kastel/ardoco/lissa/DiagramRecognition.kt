package edu.kit.kastel.ardoco.lissa

import edu.kit.kastel.ardoco.lissa.diagramrecognition.agents.DiagramRecognitionAgent
import edu.kit.kastel.ardoco.lissa.diagramrecognition.model.DiagramImpl
import edu.kit.kastel.mcse.ardoco.core.api.data.InputDiagramData
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent

class DiagramRecognition : AbstractExecutionStage {

    companion object {
        const val ID = "DiagramRecognition"
    }

    private val agents: List<PipelineAgent>
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
