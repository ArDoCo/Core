/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.docker;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages the docker containers used in InFormALin.
 */
public class DockerManager {
    private static final Logger logger = LoggerFactory.getLogger(DockerManager.class);
    private static int lastPort = 10000;
    private static final int MAX_RETRIES = 5;
    private static final long WAIT_BETWEEN_RETRIES = 10000L;

    private final String namespacePrefix;
    private final DockerAPI dockerAPI;

    /**
     * Create the manager with a container name prefix.
     *
     * @param namespacePrefix the container name prefix
     */
    public DockerManager(String namespacePrefix) {
        this(namespacePrefix, true);
    }

    /**
     * Create the manager with a container name prefix and indicator to shut down all existing containers of the
     * namespace.
     *
     * @param namespacePrefix  the container name prefix
     * @param shutdownExisting indicator whether existing containers need to be shut down at the beginning
     */
    public DockerManager(String namespacePrefix, boolean shutdownExisting) {
        this.namespacePrefix = namespacePrefix;
        this.dockerAPI = new DockerAPI();

        if (shutdownExisting)
            shutdownAll();
    }

    /**
     * Create a new container and bind the api port to {@code 127.0.0.1:$apiPort}.
     *
     * @param image the image name (with or without tag)
     * @return the container information
     */
    public ContainerResponse createContainerByImage(String image) {
        return this.createContainerByImage(image, true, true);
    }

    /**
     * Create a new container and bind the api port to {@code 127.0.0.1:$apiPort}.
     *
     * @param image                    the image name (with or without tag)
     * @param pullOnlyIfImageMissing   indicator whether pull shall only be executed if image is missing
     * @param waitForEndpointAvailable indicator whether the method shall wait until the endpoint is available
     * @return the container information
     */
    public ContainerResponse createContainerByImage(String image, boolean pullOnlyIfImageMissing, boolean waitForEndpointAvailable) {
        return createContainerByImage(image, -1, pullOnlyIfImageMissing, waitForEndpointAvailable);
    }

    /**
     * Create a new container and bind the api port to {@code 127.0.0.1:$apiPort}.
     *
     * @param image                    the image name (with or without tag)
     * @param targetPort               the API port to be exposed (if 0 or negative, the port will be determined
     *                                 automatically)
     * @param pullOnlyIfImageMissing   indicator whether pull shall only be executed if image is missing
     * @param waitForEndpointAvailable indicator whether the method shall wait until the endpoint is available
     * @return the container information
     */
    public ContainerResponse createContainerByImage(String image, int targetPort, boolean pullOnlyIfImageMissing, boolean waitForEndpointAvailable) {
        boolean pull = true;
        if (pullOnlyIfImageMissing) {
            boolean imagePresent = dockerAPI.listImagesCmd().stream().anyMatch(it -> it.repositoryWithTag().equals(image));
            if (imagePresent) {
                logger.debug("Image {} already present. Not pulling!", image);
                pull = false;
            }
        }

        if (pull) {
            dockerAPI.pullImageCmd(image);
        }

        int port;
        if (targetPort <= 0) {
            var config = this.dockerAPI.inspectImageCmd(image);
            var ports = config == null ? null : config.exposedPorts();
            if (ports == null || ports.size() != 1) {
                throw new IllegalArgumentException("Image does not expose exactly one port");
            }
            port = ports.get(0);
        } else {
            port = targetPort;
        }

        int apiPort = getNextFreePort();
        DockerPortBind dpb = new DockerPortBind(apiPort, port, false);
        String id = dockerAPI.createContainer(namespacePrefix + UUID.randomUUID(), image, dpb);
        logger.info("Created container {}", id);

        if (waitForEndpointAvailable)
            waitForAPI(apiPort);

        return new ContainerResponse(id, apiPort);
    }

    private void waitForAPI(int apiPort) {
        for (int currentTry = 0; currentTry < MAX_RETRIES; currentTry++) {
            try (var client = HttpClients.createDefault()) {
                var httpResponse = client.execute(new HttpGet("http://127.0.0.1:" + apiPort));
                if (HttpStatus.SC_SUCCESS == httpResponse.getCode()) {
                    return;
                }
            } catch (IOException e) {
                logger.debug(e.getMessage(), e);
                try {
                    Thread.sleep(WAIT_BETWEEN_RETRIES);
                } catch (InterruptedException ex) {
                    logger.error(ex.getMessage(), ex);
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new IllegalStateException("Container was not ready. Abort.");
    }

    /**
     * Shutdown and cleanup a container by id.
     *
     * @param id the container id
     */
    public void shutdown(String id) {
        var running = this.dockerAPI.listContainersCmd(false);
        if (running.stream().anyMatch(c -> c.id().equals(id)))
            this.dockerAPI.killContainerCmd(id);

        var existing = this.dockerAPI.listContainersCmd(true);
        if (existing.stream().anyMatch(c -> c.id().equals(id)))
            this.dockerAPI.removeContainerCmd(id);
    }

    /**
     * Shutdown and cleanup all containers w.r.t. the namespacePrefix
     */
    public void shutdownAll() {
        var containers = this.dockerAPI.listContainersCmd(true);
        for (var container : containers) {
            var name = container.name();
            if (name != null && name.startsWith("/" + namespacePrefix)) {
                logger.info("Shutting down {}", container);
                if (container.isRunning())
                    this.dockerAPI.killContainerCmd(container.id());
                this.dockerAPI.removeContainerCmd(container.id());
            }
        }
    }

    /**
     * Get all container ids managed by this docker manager.
     *
     * @return all container ids
     */
    public List<String> getContainerIds() {
        var containers = this.dockerAPI.listContainersCmd(true);
        return containers.stream().filter(c -> c.name() != null && c.name().startsWith("/" + namespacePrefix)).map(DockerContainer::id).toList();
    }

    /**
     * Returns a free local unprivileged port.
     *
     * @return the free port
     * @throws IllegalStateException if no port is available
     */
    public static synchronized int getNextFreePort() {
        var ports = IntStream.range(lastPort + 1, 20000).toArray();
        for (int port : ports) {
            try (ServerSocket ignored = new ServerSocket(port)) {
                lastPort = port;
                return port;
            } catch (IOException ex) {
                logger.debug("Port {} is not free.", port);
            }
        }
        // if the program gets here, no port in the range was found
        throw new IllegalStateException("no free port found");
    }
}
