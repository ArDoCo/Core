package edu.kit.kastel.ardoco.lissa.diagramrecognition.informants

import com.fasterxml.jackson.databind.ObjectMapper
import edu.kit.kastel.ardoco.lissa.diagramrecognition.createObjectMapper
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant
import edu.kit.kastel.mcse.ardoco.docker.ContainerResponse
import edu.kit.kastel.mcse.ardoco.docker.DockerManager
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClients
import java.io.IOException
import java.util.*
import java.util.stream.IntStream

abstract class DockerInformant : Informant {

    companion object {
        // E.g., 127.0.0.1
        val REMOTE_DOCKER_IP: String? = System.getenv("REMOTE_DOCKER_IP")

        // E.g., 2375
        val REMOTE_DOCKER_PORT: Int? = System.getenv("REMOTE_DOCKER_PORT")?.toIntOrNull()

        val REMOTE = REMOTE_DOCKER_IP != null && REMOTE_DOCKER_PORT != null
    }

    protected val oom: ObjectMapper = createObjectMapper()
    private val image: String
    private val defaultPort: Int
    private val useDocker: Boolean

    private val docker: DockerManager?

    constructor(
        image: String,
        defaultPort: Int,
        useDocker: Boolean,
        id: String,
        dataRepository: DataRepository,
    ) : super(id, dataRepository) {
        this.image = image
        this.defaultPort = defaultPort
        this.useDocker = useDocker
        docker = if (useDocker) initDocker() else null
    }

    private fun initDocker(): DockerManager {
        return if (REMOTE) {
            // Use Remote Docker as it is faster (can only be used by admins of the ArDoCo Organization)
            logger.debug("Use Docker Remote ..")
            DockerManager(REMOTE_DOCKER_IP!!, REMOTE_DOCKER_PORT!!, "lissa", true)
        } else {
            DockerManager("lissa", true)
        }
    }

    fun hostIp() = if (REMOTE) REMOTE_DOCKER_IP else "127.0.0.1"

    protected lateinit var container: ContainerResponse

    fun start() {
        if (useDocker) {
            this.container = docker!!.createContainerByImage(image, true, false)
        } else {
            this.container = ContainerResponse("", defaultPort)
        }
    }

    fun stop() {
        if (useDocker && this::container.isInitialized) {
            this.docker!!.shutdown(container.containerId)
        }
    }

    protected fun ensureReadiness(entryPoint: String) {
        val tries = 15
        val waiting = 10000L

        HttpClients.createDefault().use { client ->
            for (currentTry in IntStream.range(0, tries)) {
                try {
                    val get =
                        if (!REMOTE) HttpGet("http://127.0.0.1:${container.apiPort}/$entryPoint/") else HttpGet("http://$REMOTE_DOCKER_IP:${container.apiPort}/$entryPoint/")
                    val response = client.execute(get)
                    val responseEntity = response?.entity
                    val data = when (val contentStream = responseEntity?.content) {
                        null -> ""
                        else -> Scanner(contentStream).useDelimiter("\\A").use { it.next() } ?: ""
                    }
                    if (data.startsWith("Hello from ")) return
                } catch (e: IOException) {
                    logger.debug("${e.message} -- Try $currentTry", e)
                    Thread.sleep(waiting)
                }
            }
        }
        error("Cannot ensure that sketch service is running.")
    }
}
