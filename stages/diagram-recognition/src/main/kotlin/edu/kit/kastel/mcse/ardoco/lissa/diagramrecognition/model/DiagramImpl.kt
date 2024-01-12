package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.model

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Connector
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox
import java.io.File
import java.util.Objects

class DiagramImpl :
    Diagram {
    private val resourceName: String
    private val location: File

    constructor(resourceName: String, location: File) {
        this.resourceName = resourceName
        this.location = location
        this.boxes = mutableListOf()
        this.textBoxes = mutableListOf()
        this.connectors = mutableListOf()
    }

    private val boxes: MutableList<Box>
    private val textBoxes: MutableList<TextBox>
    private val connectors: MutableList<Connector>

    private constructor() : this("", File("")) {
    }

    override fun getResourceName(): String {
        return resourceName
    }

    override fun getLocation(): File = location

    override fun addBox(box: Box) {
        boxes.add(box)
    }

    override fun addTextBox(textBox: TextBox) {
        textBoxes.add(textBox)
    }

    override fun addConnector(connector: Connector) {
        connectors.add(connector)
    }

    override fun removeBox(box: Box): Boolean {
        return boxes.remove(box)
    }

    override fun removeTextBox(textBox: TextBox): Boolean {
        return textBoxes.remove(textBox)
    }

    override fun removeConnector(connector: Connector): Boolean {
        return connectors.remove(connector)
    }

    override fun getBoxes(): MutableList<Box> = boxes.toMutableList()

    override fun getTextBoxes(): MutableList<TextBox> = textBoxes.toMutableList()

    override fun getConnectors(): MutableList<Connector> = connectors.toMutableList()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is Diagram) {
            return boxes == other.boxes && textBoxes == other.textBoxes && connectors == other.connectors
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(boxes, textBoxes, connectors)
    }
}
