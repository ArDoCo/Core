package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants

import com.fasterxml.jackson.databind.ObjectMapper
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant
import edu.kit.kastel.mcse.ardoco.docker.ContainerResponse
import edu.kit.kastel.mcse.ardoco.docker.DockerManager
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.createObjectMapper
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler
import org.apache.hc.client5.http.impl.classic.HttpClients
import java.io.IOException
import java.util.stream.IntStream

abstract class DockerInformant : Informant {
    companion object {
        // E.g., 127.0.0.1
        private val REMOTE_DOCKER_IP: String? = System.getenv("REMOTE_DOCKER_IP")

        // E.g., 2375
        private val REMOTE_DOCKER_PORT: Int? = System.getenv("REMOTE_DOCKER_PORT")?.toIntOrNull()

        private val REMOTE = REMOTE_DOCKER_IP != null && REMOTE_DOCKER_PORT != null
        private val dockerManagerCache: MutableMap<String, DockerManager> = mutableMapOf()

        /**
         * Create a DockerManager from Namespace (or load from cache).
         * @param[namespace] the namespace to use
         */
        @Synchronized
        protected fun createDockerManager(namespace: String): DockerManager {
            if (namespace in dockerManagerCache.keys) {
                return dockerManagerCache[namespace]!!
            }
            val manager =
                if (REMOTE) {
                    // Use Remote Docker as it is faster (can only be used by admins of the ArDoCo Organization)
                    DockerManager(REMOTE_DOCKER_IP!!, REMOTE_DOCKER_PORT!!, "lissa", true)
                } else {
                    DockerManager("lissa", true)
                }
            dockerManagerCache[namespace] = manager
            return manager
        }
    }

    /**
     * A configured object mapper for serialization / deserialization of objects.
     */
    protected val oom: ObjectMapper = createObjectMapper()
    private val image: String
    private val defaultPort: Int
    private val useDocker: Boolean

    private var dockerManager: DockerManager? = null

    /**
     * Create the docker informant.
     * @param[image] the docker image to use
     * @param[defaultPort] the default port of the image's service
     * @param[useDocker] whether or not to use the docker image (just for debugging)
     * @param[id] the id of the informant
     * @param[dataRepository] the data repository of the informant
     */
    protected constructor(
        image: String,
        defaultPort: Int,
        useDocker: Boolean,
        id: String,
        dataRepository: DataRepository
    ) : super(id, dataRepository) {
        this.image = image
        this.defaultPort = defaultPort
        this.useDocker = useDocker
    }

    /**
     * Get the host IP to connect
     * @return the IP of the host to connect
     */
    protected fun hostIP() = if (REMOTE) REMOTE_DOCKER_IP else "127.0.0.1"

    /**
     * The information about the spawned container (e.g., the information about the port mapping)
     */
    protected lateinit var container: ContainerResponse

    /**
     * Start the container.
     */
    protected fun start() {
        if (useDocker) {
            this.container = docker().createContainerByImage(image, true, false)
        } else {
            this.container = ContainerResponse("", defaultPort)
        }
    }

    /**
     * Stop the container.
     */
    protected fun stop() {
        if (useDocker && this::container.isInitialized) {
            this.docker().shutdown(container.containerId)
        }
    }

    private fun docker(): DockerManager {
        if (!useDocker) {
            error("Try to get docker while docker is disabled")
        }
        if (dockerManager != null) {
            return dockerManager!!
        }
        dockerManager = createDockerManager("lissa")
        return dockerManager!!
    }

    /**
     * Ensure the readiness of the container or service by its entrypoint (e.g., "ocr" to access "http://IP:Port/ocr").
     * @throws[IllegalStateException] if failed after multiple retries
     */
    protected fun ensureReadiness(entryPoint: String) {
        val tries = 15
        val waiting = 10000L

        HttpClients.createDefault().use { client ->
            for (currentTry in IntStream.range(0, tries)) {
                try {
                    val get = HttpGet("http://${hostIP()}:${container.apiPort}/$entryPoint/")
                    val data = client.execute(get, BasicHttpClientResponseHandler())
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
