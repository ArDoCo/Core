package edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.model

import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.Connector
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.TextBox

class DiagramImpl(private val location: String, private val sketchRecognitionResult: SketchRecognitionResult) :
    Diagram {

    override fun getLocation(): String = location

    override fun getBoxes(): MutableList<Box> = sketchRecognitionResult.boxes.toMutableList()

    override fun getTextBoxes(): MutableList<TextBox> = sketchRecognitionResult.textBoxes.toMutableList()

    override fun getConnectors(): MutableList<Connector> = sketchRecognitionResult.edges.toMutableList()
}
