package edu.kit.kastel.ardoco.lissa.diagramrecognition.informants

import com.fasterxml.jackson.module.kotlin.readValue
import edu.kit.kastel.ardoco.lissa.diagramrecognition.executeRequest
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.TextBox
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.net.URIBuilder
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI

class OCRInformant(dataRepository: DataRepository) : ImageProcessingDockerInformant(
    DOCKER_OCR,
    DEFAULT_PORT,
    DOCKER_OCR_VIA_DOCKER,
    ID,
    dataRepository,
    "ocr",
) {
    companion object {
        const val ID = "OCRInformant"
        const val DOCKER_OCR = "ghcr.io/lissa-approach/diagram-ocr:latest"
        const val DEFAULT_PORT = 5005
        const val DOCKER_OCR_VIA_DOCKER = true

        const val MINIMUM_AREA_IN_PXPX = 20
        const val EXPANSION_IN_PX = 5
    }

    override fun delegateApplyConfigurationToInternalObjects(additionalConfiguration: MutableMap<String, String>?) {
        // Not needed
    }

    override fun processImage(diagram: Diagram, imageData: ByteArray) {
        val textsWithHints = detectTextBoxes(ByteArrayInputStream(imageData), diagram.boxes.filter { it.classification == "Label" })
        val textsWithoutHints = detectTextBoxes(ByteArrayInputStream(imageData), listOf())
        val texts = mergeTexts(textsWithHints, textsWithoutHints)
        texts.forEach { diagram.addTextBox(it) }
    }

    private fun mergeTexts(textsWithHints: List<TextBox>, textsWithoutHints: List<TextBox>): List<TextBox> {
        logger.debug("Merging ${textsWithHints.size} TextsWithHint and ${textsWithoutHints.size} TextsWithoutHint")
        // TODO Impl
        return textsWithHints
    }

    private fun detectTextBoxes(image: InputStream, detectedBoxesOfObjectDetection: List<Box>): List<TextBox> {
        val textRecognition = sendOCRRequest(
            image,
            container.apiPort,
            detectedBoxesOfObjectDetection.filter { it.classification == "Label" },
        )
        logger.debug("Processed OCRService Request")
        return oom.readValue(textRecognition)
    }

    private fun sendOCRRequest(image: InputStream, port: Int, labels: List<Box>): String {
        val boxCoordinates = enhanceLabels(labels).flatMap { it.box.toList() }.joinToString(",")

        val builder = MultipartEntityBuilder.create()
        builder.addBinaryBody("file", image, ContentType.APPLICATION_OCTET_STREAM, "image")
        val multipart: HttpEntity = builder.build()
        val uploadFile = HttpPost("http://${hostIP()}:$port/ocr/")
        if (labels.isNotEmpty()) {
            val uri: URI = URIBuilder(uploadFile.uri).addParameter("regions", boxCoordinates).build()
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
        val newPositions = listOf(
            box.box[0] - EXPANSION_IN_PX,
            box.box[1] - EXPANSION_IN_PX,
            box.box[2] + EXPANSION_IN_PX,
            box.box[3] + EXPANSION_IN_PX,
        )
        // Copy References here. No Copies!
        return Box(box.uuid, newPositions.toIntArray(), box.confidence, box.classification, box.texts, null)
    }
}
