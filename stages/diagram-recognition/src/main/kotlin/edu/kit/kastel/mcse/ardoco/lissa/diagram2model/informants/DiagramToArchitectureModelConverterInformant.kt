package edu.kit.kastel.mcse.ardoco.lissa.diagram2model.informants

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureModel
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.models.ModelExtractionStateImpl
import edu.kit.kastel.mcse.ardoco.core.models.ModelInstanceImpl
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant
import org.eclipse.collections.api.factory.Lists
import java.util.SortedMap

class DiagramToArchitectureModelConverterInformant(dataRepository: DataRepository) : Informant(ID, dataRepository) {
    companion object {
        const val ID = "DiagramToArchitectureModelConverterInformant"
    }

    override fun delegateApplyConfigurationToInternalObjects(additionalConfiguration: SortedMap<String, String>?) {
        // Not needed
    }

    override fun run() {
        val diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(dataRepository)
        val architectureState = DataRepositoryHelper.getModelStatesData(dataRepository)
        moveDiagramsToArchitectureState(diagramRecognitionState, architectureState)
    }

    private fun moveDiagramsToArchitectureState(diagramRecognitionState: DiagramRecognitionState, architectureState: ModelStates) {
        val diagramElements = mutableListOf<ModelInstance>()
        addDiagrams(diagramRecognitionState, diagramElements)

        val architecture = ModelExtractionStateImpl("diagram-architecture", Metamodel.ARCHITECTURE, Lists.immutable.withAll(diagramElements))
        architectureState.addModelExtractionState(architecture.modelId, architecture)

        val architectureModel = ArchitectureModel(diagramElements.map { ArchitectureComponent(it.name, it.id, sortedSetOf(), sortedSetOf(), sortedSetOf()) })
        architectureState.addModel("PCM", architectureModel)
    }

    private fun addDiagrams(diagramRecognitionState: DiagramRecognitionState, diagramElements: MutableList<ModelInstance>) {
        for (diagram in diagramRecognitionState.diagrams) {
            for (diagramElement in diagram.boxes) {
                // For now a box will be a component
                val name = diagramElement.texts.sortedBy { it.yCoordinate }.joinToString("") { it.text }.trim()
                if (name.isBlank()) {
                    continue
                }
                val component = ModelInstanceImpl(name, "component", diagramElement.uuid)
                diagramElements.add(component)
            }
        }
    }
}
