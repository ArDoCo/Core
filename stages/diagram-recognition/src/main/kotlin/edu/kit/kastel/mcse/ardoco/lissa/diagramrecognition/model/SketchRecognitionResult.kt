package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.model

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Connector
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox

data class SketchRecognitionResult(val boxes: List<Box>, val textBoxes: List<TextBox>, val edges: List<Connector>)
