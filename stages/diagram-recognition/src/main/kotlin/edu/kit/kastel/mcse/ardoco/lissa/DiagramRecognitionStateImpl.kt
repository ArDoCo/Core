package edu.kit.kastel.mcse.ardoco.lissa

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState

class DiagramRecognitionStateImpl : DiagramRecognitionState {
    private val diagrams = mutableListOf<Diagram>()
    private val unprocessedDiagrams = mutableListOf<Diagram>()
    private val disambiguations = mutableListOf<Disambiguation>()

    override fun addUnprocessedDiagram(diagram: Diagram) {
        unprocessedDiagrams.add(diagram)
    }

    override fun getUnprocessedDiagrams(): List<Diagram> = unprocessedDiagrams.toList()

    override fun removeUnprocessedDiagram(diagram: Diagram): Boolean =
        unprocessedDiagrams
            .remove(diagram)

    override fun addDiagram(diagram: Diagram) {
        diagrams.add(diagram)
    }

    override fun getDiagrams(): MutableList<Diagram> = diagrams.toMutableList()

    override fun addDisambiguation(disambiguation: Disambiguation): Boolean {
        return disambiguations.add(disambiguation)
    }

    override fun getDisambiguations(): MutableList<Disambiguation> = disambiguations.toMutableList()
}
