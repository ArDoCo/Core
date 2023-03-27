/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.docker;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;

public final class DockerAPI {

    private static final Logger logger = LoggerFactory.getLogger(DockerAPI.class);
    private final DockerClient docker;

    public DockerAPI() {
        DockerClient dockerInstance = null;
        try {
            DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
            DockerHttpClient dhc = new ZerodepDockerHttpClient.Builder().dockerHost(config.getDockerHost()).build();
            dockerInstance = DockerClientImpl.getInstance(config, dhc);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        this.docker = dockerInstance;
        this.checkDockerExistence();
    }

    public List<DockerImage> listImagesCmd() {
        try {
            var images = docker.listImagesCmd().withShowAll(true).exec();

            return images.stream() //
                    .map(image -> new DockerImage(image.getParentId(), image.getRepoTags()[0], null))
                    .filter(it -> !it.isNone())
                    .toList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return List.of();
        }
    }

    public boolean pullImageCmd(String image) {
        try {
            docker.pullImageCmd(image).start().awaitCompletion();
            return true;
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public List<DockerContainer> listContainersCmd(boolean showAll) {
        List<Container> containers = new ArrayList<>();
        try {
            containers = docker.listContainersCmd().withShowAll(showAll).exec();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return containers.stream()
                .map(container -> new DockerContainer(container.getId(), container.getImage(), container.getStatus(), container.getNames()[0]))
                .toList();
    }

    public DockerImage inspectImageCmd(String image) {
        try {
            var imageInspect = docker.inspectImageCmd(image).exec();
            return new DockerImage(//
                    Objects.requireNonNull(imageInspect.getRepoTags()).get(0), //
                    imageInspect.getRepoTags().get(0), //
                    Arrays.stream(Objects.requireNonNull(imageInspect.getConfig()).getExposedPorts()).map(ExposedPort::getPort).toList() //
            );
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public String createContainer(String name, String image, DockerPortBind dpb) {
        if (!dpb.valid()) {
            logger.error("DockerPortBind is invalid!");
            throw new IllegalArgumentException("Invalid Docker Port Binding");
        }

        var binding = (dpb.wildcard() ? "0.0.0.0:" : "127.0.0.1:") + dpb.hostPort() + ":" + dpb.containerPort();

        try (var command = docker.createContainerCmd(image)) {
            var container = command.withName(name).withHostConfig(HostConfig.newHostConfig().withPortBindings(PortBinding.parse(binding))).exec();
            docker.startContainerCmd(container.getId()).exec();
            return container.getId();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean killContainerCmd(String id) {
        try {
            docker.killContainerCmd(id).exec();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean removeContainerCmd(String id) {
        try {
            docker.removeContainerCmd(id).exec();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    private void checkDockerExistence() {
        if (docker == null)
            throw new IllegalArgumentException("Could not connect to Docker");
        var version = docker.versionCmd().exec();
        logger.info("Connected to Docker: {}", version);
    }
}
