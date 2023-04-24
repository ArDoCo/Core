package edu.kit.kastel.ardoco.lissa.diagramrecognition.model

import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.Connector
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.TextBox

data class SketchRecognitionResult(val boxes: List<Box>, val textBoxes: List<TextBox>, val edges: List<Connector>)
