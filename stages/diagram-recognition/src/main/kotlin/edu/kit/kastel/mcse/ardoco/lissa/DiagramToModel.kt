package edu.kit.kastel.mcse.ardoco.lissa

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractExecutionStage
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent
import edu.kit.kastel.mcse.ardoco.lissa.diagram2model.agents.DiagramToArchitectureModelConverterAgent

class DiagramToModel : AbstractExecutionStage {

    companion object {
        const val ID = "DiagramToModel"

        /**
         * Creates a [DiagramToModel] that will handle the diagram to model transformation.
         *
         * @param additionalConfigs the additional configuration that should be applied
         * @param dataRepository    the data repository
         * @return a DiagramToModel processing with the provided diagrams
         */
        @JvmStatic
        fun get(additionalConfigs: Map<String?, String?>?, dataRepository: DataRepository?): DiagramToModel? {
            val diagramToModel = DiagramToModel(dataRepository!!)
            diagramToModel.applyConfiguration(additionalConfigs)
            return diagramToModel
        }
    }

    private val agents: List<PipelineAgent>

    @Configurable
    private var enabledAgents: MutableList<String>

    constructor(dataRepository: DataRepository) : super(ID, dataRepository) {
        this.agents = listOf(DiagramToArchitectureModelConverterAgent(dataRepository))
        enabledAgents = this.agents.map { it.id }.toMutableList()
    }

    override fun initializeState() {
        if (!DataRepositoryHelper.hasModelStatesData(dataRepository)) {
            dataRepository.addData(ModelStates.ID, ModelStates())
        }
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
