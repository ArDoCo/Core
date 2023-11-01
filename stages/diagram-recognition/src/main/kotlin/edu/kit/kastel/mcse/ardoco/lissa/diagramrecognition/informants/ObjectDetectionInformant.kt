package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants

import com.fasterxml.jackson.module.kotlin.readValue
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.executeRequest
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.HttpEntity
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.SortedMap

class ObjectDetectionInformant(
    dataRepository: DataRepository
) : ImageProcessingDockerInformant(
        DOCKER_SKETCH_RECOGNITION,
        DEFAULT_PORT,
        DOCKER_SKETCH_RECOGNITION_VIA_DOCKER,
        ID,
        dataRepository,
        "sketches"
    ) {
    companion object {
        const val DOCKER_SKETCH_RECOGNITION = "ghcr.io/lissa-approach/detectron2-sr:latest"
        const val DEFAULT_PORT = 5005
        const val DOCKER_SKETCH_RECOGNITION_VIA_DOCKER = true

        const val ID = "ObjectDetectionInformant"
    }

    override fun delegateApplyConfigurationToInternalObjects(additionalConfiguration: SortedMap<String, String>?) {
        // Not needed
    }

    override fun processImage(
        diagram: Diagram,
        imageData: ByteArray
    ) {
        val boxes = detectEntities(diagram, ByteArrayInputStream(imageData))
        boxes.forEach { diagram.addBox(it) }
    }

    fun detectEntities(
        diagram: Diagram,
        image: InputStream
    ): List<Box> {
        val sketchRecognition = sendSketchRecognitionRequest(image)
        logger.debug("Processed DiagramRecognition request")
        return oom.readValue(sketchRecognition)
    }

    private fun sendSketchRecognitionRequest(image: InputStream): String {
        // Create Request
        val builder = MultipartEntityBuilder.create()
        builder.addBinaryBody("file", image, ContentType.APPLICATION_OCTET_STREAM, "image")
        val uploadFile = HttpPost(getUri())
        val multipart: HttpEntity = builder.build()
        uploadFile.entity = multipart
        return executeRequest(uploadFile, true)
    }
}
