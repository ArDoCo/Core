package edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.services

import com.fasterxml.jackson.module.kotlin.readValue
import edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.SketchRecognitionService
import edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.executeRequest
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.TextBox
import edu.kit.kastel.mcse.ardoco.docker.DockerManager
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.net.URIBuilder
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URI

class OCRService(docker: DockerManager) : DockerSubService(docker, DOCKER_OCR, DEFAULT_PORT, DOCKER_OCR_VIA_DOCKER) {
    companion object {
        const val DOCKER_OCR = "ghcr.io/lissa-approach/diagram-ocr:latest"
        const val DEFAULT_PORT = 5005
        const val DOCKER_OCR_VIA_DOCKER = true
    }

    fun recognize(byteData: ByteArray, boxes: List<Box>): List<TextBox> {
        ensureReadiness("ocr")

        val textRecognition = sendOCRRequest(
            ByteArrayInputStream(byteData),
            container.apiPort,
            boxes.filter { it.classification == "Label" },
        )
        logger.debug("Processed OCRService Request")
        return oom.readValue(textRecognition)
    }

    private fun sendOCRRequest(image: InputStream, port: Int, labels: List<Box>): String {
        val boxCoordinates = enhanceLabels(labels).flatMap { it.box.toList() }.joinToString(",")

        val builder = MultipartEntityBuilder.create()
        builder.addBinaryBody("file", image, ContentType.APPLICATION_OCTET_STREAM, "image")
        val multipart: HttpEntity = builder.build()
        val uploadFile = HttpPost("http://127.0.0.1:$port/ocr/")
        if (labels.isNotEmpty()) {
            val uri: URI = URIBuilder(uploadFile.uri).addParameter("regions", boxCoordinates).build()
            uploadFile.uri = uri
        }
        uploadFile.entity = multipart
        return executeRequest(uploadFile)
    }

    private fun enhanceLabels(labels: List<Box>): List<Box> {
        val result = labels.filter { it.area() > SketchRecognitionService.MINIMUM_AREA_IN_PXPX }.toMutableList()

        for (idx in result.indices) {
            result[idx] = expandPixels(result[idx])
        }

        return result
    }

    private fun expandPixels(box: Box): Box {
        // TODO Better expansion mechanism based on area
        val newPositions = listOf(
            box.box[0] - SketchRecognitionService.EXPANSION_IN_PX,
            box.box[1] - SketchRecognitionService.EXPANSION_IN_PX,
            box.box[2] + SketchRecognitionService.EXPANSION_IN_PX,
            box.box[3] + SketchRecognitionService.EXPANSION_IN_PX,
        )
        // Copy References here. No Copies!
        return Box(box.uuid, newPositions.toIntArray(), box.confidence, box.classification, box.texts, null)
    }
}
