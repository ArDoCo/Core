package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants

import com.fasterxml.jackson.module.kotlin.readValue
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Classification
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.boundingBox
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.executeRequest
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.net.URIBuilder
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI
import java.util.SortedMap

class OcrInformant(
    dataRepository: DataRepository
) : ImageProcessingDockerInformant(
        DOCKER_OCR,
        DEFAULT_PORT,
        DOCKER_OCR_VIA_DOCKER,
        ID,
        dataRepository,
        "ocr"
    ) {
    companion object {
        const val ID = "OCRInformant"
        const val DOCKER_OCR = "ghcr.io/lissa-approach/diagram-ocr:latest"
        const val DEFAULT_PORT = 5005
        const val DOCKER_OCR_VIA_DOCKER = true

        const val MINIMUM_AREA_IN_PXPX = 20
        const val EXPANSION_IN_PX = 5
    }

    @Configurable
    private var detectionWithHintThreshold: Double = 0.6

    @Configurable
    private var detectionWithoutHintThreshold: Double = 0.4

    @Configurable
    private var iouThreshold: Double = 0.6

    override fun delegateApplyConfigurationToInternalObjects(additionalConfiguration: SortedMap<String, String>?) {
        // Not needed
    }

    override fun processImage(
        diagram: Diagram,
        imageData: ByteArray
    ) {
        val textsWithHints =
            detectTextBoxes(
                ByteArrayInputStream(imageData),
                diagram.boxes.filter { it.classification == Classification.LABEL }
            )
        val textsWithoutHints = detectTextBoxes(ByteArrayInputStream(imageData), listOf())
        val texts = mergeTexts(textsWithHints, textsWithoutHints)
        texts.forEach { diagram.addTextBox(it) }
    }

    private fun mergeTexts(
        textsWithHints: List<TextBox>,
        textsWithoutHints: List<TextBox>
    ): List<TextBox> {
        logger.debug("Merging ${textsWithHints.size} TextsWithHint and ${textsWithoutHints.size} TextsWithoutHint")

        val filteredWithHint = textsWithHints.filter { it.confidence > detectionWithHintThreshold }.sortedByDescending { it.confidence }
        val filteredWithoutHint = textsWithoutHints.filter { it.confidence > detectionWithoutHintThreshold }.sortedByDescending { it.confidence }

        val result = mutableListOf<TextBox>()
        for (textBox in filteredWithHint + filteredWithoutHint) {
            val intersections = result.map { it to it.absoluteBox().boundingBox().iou(textBox.absoluteBox().boundingBox()) }
            val filteredIntersections = intersections.filter { it.second.iou > iouThreshold }
            if (filteredIntersections.isEmpty()) {
                result.add(textBox)
            }
        }

        return result
    }

    private fun detectTextBoxes(
        image: InputStream,
        detectedBoxesOfObjectDetection: List<Box>
    ): List<TextBox> {
        val textRecognition =
            sendOCRRequest(
                image,
                detectedBoxesOfObjectDetection.filter { it.classification == Classification.LABEL }
            )
        logger.debug("Processed OCRService Request")
        return oom.readValue(textRecognition)
    }

    private fun sendOCRRequest(
        image: InputStream,
        labels: List<Box>
    ): String {
        val boxCoordinates = enhanceLabels(labels).flatMap { it.box.toList() }.joinToString(",")

        val builder = MultipartEntityBuilder.create()
        builder.addBinaryBody("file", image, ContentType.APPLICATION_OCTET_STREAM, "image")
        val multipart: HttpEntity = builder.build()
        val uploadFile = HttpPost(getUri())
        if (labels.isNotEmpty()) {
            val uri: URI =
                URIBuilder(uploadFile.uri).addParameter("regions", boxCoordinates).build()
            uploadFile.uri = uri
        }
        uploadFile.entity = multipart
        return executeRequest(uploadFile)
    }

    private fun enhanceLabels(labels: List<Box>): List<Box> {
        val result = labels.filter { it.area() > MINIMUM_AREA_IN_PXPX }.toMutableList()

        for (idx in result.indices) {
            result[idx] = expandPixels(result[idx])
        }

        return result
    }

    private fun expandPixels(box: Box): Box {
        // TODO Better expansion mechanism based on area
        val newPositions =
            listOf(
                box.box[0] - EXPANSION_IN_PX,
                box.box[1] - EXPANSION_IN_PX,
                box.box[2] + EXPANSION_IN_PX,
                box.box[3] + EXPANSION_IN_PX
            )
        // Copy References here. No Copies!
        return Box(
            box.diagram,
            newPositions.toIntArray(),
            box.confidence,
            box.classification
                .classificationString,
            box.texts,
            null
        )
    }
}
