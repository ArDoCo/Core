package edu.kit.kastel.ardoco.lissa

import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.DiagramRecognitionState

class DiagramRecognitionStateImpl : DiagramRecognitionState {
    private val diagrams = mutableListOf<Diagram>()
    override fun addDiagram(diagram: Diagram) {
        diagrams.add(diagram)
    }

    override fun getDiagrams(): MutableList<Diagram> = diagrams.toMutableList()
}
