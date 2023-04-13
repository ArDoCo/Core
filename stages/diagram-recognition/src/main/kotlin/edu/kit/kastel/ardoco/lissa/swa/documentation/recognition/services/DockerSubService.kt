package edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.services

import edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.SketchRecognitionService
import edu.kit.kastel.ardoco.lissa.utils.createObjectMapper
import edu.kit.kastel.mcse.ardoco.docker.ContainerResponse
import edu.kit.kastel.mcse.ardoco.docker.DockerManager
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.Scanner
import java.util.stream.IntStream

abstract class DockerSubService(
    private val docker: DockerManager,
    private val image: String,
    private val defaultPort: Int,
    private val useDocker: Boolean,
) {
    protected val logger: Logger = LoggerFactory.getLogger(this::class.java)
    protected val oom = createObjectMapper()

    protected lateinit var container: ContainerResponse

    fun start() {
        if (useDocker) {
            this.container = docker.createContainerByImage(image, true, false)
        } else {
            this.container = ContainerResponse("", defaultPort)
        }
    }

    fun stop() {
        if (useDocker && this::container.isInitialized) {
            this.docker.shutdown(container.containerId)
        }
    }

    protected fun ensureReadiness(entryPoint: String) {
        val tries = 15
        val waiting = 10000L

        HttpClients.createDefault().use { client ->
            for (currentTry in IntStream.range(0, tries)) {
                try {
                    val response = client.execute(HttpGet("http://127.0.0.1:${container.apiPort}/$entryPoint/"))
                    val responseEntity = response?.entity
                    val data = when (val contentStream = responseEntity?.content) {
                        null -> ""
                        else -> Scanner(contentStream).useDelimiter("\\A").use { it.next() } ?: ""
                    }
                    if (data.startsWith("Hello from ")) return
                } catch (e: IOException) {
                    SketchRecognitionService.logger.debug("${e.message} -- Try $currentTry", e)
                    Thread.sleep(waiting)
                }
            }
        }
        error("Cannot ensure that sketch service is running.")
    }
}
