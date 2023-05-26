package edu.kit.kastel.mcse.ardoco.lissa

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState

class DiagramRecognitionStateImpl : DiagramRecognitionState {
    private val diagrams = mutableListOf<Diagram>()
    override fun addDiagram(diagram: Diagram) {
        diagrams.add(diagram)
    }

    override fun getDiagrams(): MutableList<Diagram> = diagrams.toMutableList()
}
