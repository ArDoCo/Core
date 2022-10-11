/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textclassification;

import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.informalin.framework.docker.ContainerResponse;
import edu.kit.kastel.informalin.framework.docker.DockerManager;
import edu.kit.kastel.mcse.ardoco.core.textclassification.records.*;

/**
 * ClassifierLocal implements a {@link TextClassifier} similar to {@link ClassifierNetworkAsync} by connecting
 * to an external classifier via a {@link AsyncRestAPI}. When instantiating a ClassifierLocal,
 * a Docker container is started on the local machine, in which the external classifier is deployed
 * and reached on localhost (http://127.0.0.1)
 */
public class ClassifierLocal implements TextClassifier {

    private ContainerResponse container;
    private DockerManager dockerManager;
    private String dockerImageName;
    private final TextClassifier classifier;

    private static final Logger logger = LoggerFactory.getLogger(ClassifierLocal.class);

    /**
     * @param dockerImageName the name of the external classifiers Docker Image
     * @param timeout         the maximum time to wait for API responses.
     */
    public ClassifierLocal(String dockerImageName, int timeout) {
        init(dockerImageName);
        startContainer(-1);
        this.classifier = new ClassifierNetworkAsync(new AsyncRestAPI("http://127.0.0.1", container.apiPort()), timeout);
    }

    /**
     *
     * @param dockerImageName the name of the external classifiers Docker Image
     * @param apiPort         port of the external classifiers API
     * @param timeout         the maximum time to wait for API responses.
     */
    public ClassifierLocal(String dockerImageName, int apiPort, int timeout) {
        init(dockerImageName);
        startContainer(apiPort);
        this.classifier = new ClassifierNetworkAsync(new AsyncRestAPI("http://127.0.0.1", container.apiPort()), timeout);
    }

    private void init(String dockerImageName) {
        if (dockerImageName.contains(":")) {
            this.dockerImageName = dockerImageName.split(":")[0];
            logger.warn("image tags will be ignored");

        } else {
            this.dockerImageName = dockerImageName;
        }
        this.dockerManager = new DockerManager(this.dockerImageName);
    }

    private void startContainer(int apiPort) {
        for (String id : dockerManager.getContainerIds()) {
            dockerManager.shutdown(id);
        }

        if (apiPort <= 0) {
            this.container = dockerManager.createContainerByImage(dockerImageName, true, true);
        } else {
            this.container = dockerManager.createContainerByImage(dockerImageName, apiPort, true, true);
        }
        logger.info(" successfully started container: {}", container);

    }

    @Override
    public ClassificationResponse classifyPhrases(Map<Integer, String> phrases) throws TimeoutException {
        return classifier.classifyPhrases(phrases);
    }

    @Override
    public ClassifierStatus getClassifierStatus() throws TimeoutException {
        return classifier.getClassifierStatus();
    }
}
