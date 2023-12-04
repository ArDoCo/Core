package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants

import com.fasterxml.jackson.databind.InjectableValues
import com.fasterxml.jackson.databind.ObjectMapper
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.createObjectMapper

abstract class ImageProcessingDockerInformant(
    image: String,
    defaultPort: Int,
    useDocker: Boolean,
    id: String,
    dataRepository: DataRepository,
    endpoint: String
) : DockerInformant(
        image,
        defaultPort,
        useDocker,
        id,
        dataRepository,
        endpoint
    ) {
    /**
     * A configured object mapper for serialization / deserialization of objects.
     */
    @Transient
    protected var oom: ObjectMapper = createObjectMapper()

    final override fun process() {
        try {
            start()
            ensureReadiness()
            processImages()
        } catch (e: Exception) {
            logger.error(e.message, e)
        } finally {
            stop()
        }
    }

    private fun processImages() {
        val diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(dataRepository)
        for (diagram in diagramRecognitionState.getUnprocessedDiagrams()) {
            // Inject diagram into mapper
            oom.setInjectableValues(
                InjectableValues.Std().addValue(
                    Diagram::class.java,
                    diagram
                )
            )

            logger.debug("Process {}", diagram.location)
            diagram.location.readBytes().let { imageStream ->
                processImage(diagram, imageStream)
            }
        }
    }

    protected abstract fun processImage(
        diagram: Diagram,
        imageData: ByteArray
    )
}
