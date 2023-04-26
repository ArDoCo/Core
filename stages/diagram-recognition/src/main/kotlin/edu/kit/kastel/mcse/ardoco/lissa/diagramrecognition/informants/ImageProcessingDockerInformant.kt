package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants

import edu.kit.kastel.mcse.ardoco.core.api.data.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository

abstract class ImageProcessingDockerInformant(
    image: String,
    defaultPort: Int,
    useDocker: Boolean,
    id: String,
    dataRepository: DataRepository,
    private val defaultEndpoint: String,
) : DockerInformant(
    image,
    defaultPort,
    useDocker,
    id,
    dataRepository,
) {
    final override fun run() {
        try {
            start()
            ensureReadiness(defaultEndpoint)
            processImages()
        } catch (e: Exception) {
            logger.error(e.message, e)
        } finally {
            stop()
        }
    }

    private fun processImages() {
        val diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(dataRepository)
        for (diagram in diagramRecognitionState.diagrams) {
            logger.debug("Process {}", diagram.location)
            diagram.location.readBytes().let { imageStream ->
                processImage(diagram, imageStream)
            }
        }
    }

    protected abstract fun processImage(diagram: Diagram, imageData: ByteArray)
}
