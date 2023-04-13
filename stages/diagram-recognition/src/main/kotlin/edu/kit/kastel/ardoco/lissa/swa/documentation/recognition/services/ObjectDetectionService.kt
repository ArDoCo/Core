package edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.services

import com.fasterxml.jackson.module.kotlin.readValue
import edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.executeRequest
import edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.model.Box
import edu.kit.kastel.mcse.ardoco.docker.DockerManager
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.HttpEntity
import java.io.ByteArrayInputStream
import java.io.InputStream

class ObjectDetectionService(docker: DockerManager) : DockerSubService(
    docker,
    DOCKER_SKETCH_RECOGNITION,
    DEFAULT_PORT,
    DOCKER_SKETCH_RECOGNITION_VIA_DOCKER,
) {
    companion object {
        const val DOCKER_SKETCH_RECOGNITION = "ghcr.io/lissa-approach/detectron2-sr:latest"
        const val DEFAULT_PORT = 5005
        const val DOCKER_SKETCH_RECOGNITION_VIA_DOCKER = true
    }

    fun recognize(imageData: ByteArray): List<Box> {
        ensureReadiness("sketches")

        val dataStream = ByteArrayInputStream(imageData)
        val sketchRecognition = sendSketchRecognitionRequest(dataStream)
        logger.debug("Processed DiagramRecognition request")
        return oom.readValue(sketchRecognition)
    }

    private fun sendSketchRecognitionRequest(image: InputStream): String {
        // Create Request
        val builder = MultipartEntityBuilder.create()
        builder.addBinaryBody("file", image, ContentType.APPLICATION_OCTET_STREAM, "image")
        val uploadFile = HttpPost("http://127.0.0.1:${container.apiPort}/sketches/")
        val multipart: HttpEntity = builder.build()
        uploadFile.entity = multipart
        return executeRequest(uploadFile)
    }
}
