package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.model

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Connector
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox
import java.io.File

class DiagramImpl(private val location: File) :
    Diagram {
    private val boxes: MutableList<Box> = mutableListOf()
    private val textBoxes: MutableList<TextBox> = mutableListOf()
    private val connectors: MutableList<Connector> = mutableListOf()

    private constructor() : this(File("")) {
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
}
