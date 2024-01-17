package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.model

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Connector
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox
import java.io.Serializable

data class SketchRecognitionResult(
    val boxes: List<Box>,
    val textBoxes: List<TextBox>,
    val edges: List<Connector>
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 13L
    }
}
