package edu.kit.kastel.mcse.ardoco.lissa.diagram2model.agents

import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent
import edu.kit.kastel.mcse.ardoco.lissa.diagram2model.informants.DiagramToArchitectureModelConverterInformant
import java.util.SortedMap

class DiagramToArchitectureModelConverterAgent(dataRepository: DataRepository) : PipelineAgent(ID, dataRepository) {
    companion object {
        const val ID = "DiagramToArchitectureModelConverterAgent"
    }

    private val informants = listOf(
        DiagramToArchitectureModelConverterInformant(dataRepository)
    )

    @Configurable
    private var enabledInformants: MutableList<String> =
        informants.map { e: Informant -> e.javaClass.simpleName }.toMutableList()

    override fun getEnabledPipelineSteps(): MutableList<Informant> = findByClassName(enabledInformants, informants)

    override fun delegateApplyConfigurationToInternalObjects(additionalConfiguration: SortedMap<String?, String?>?) {
        informants.forEach { it.applyConfiguration(additionalConfiguration) }
    }
}
