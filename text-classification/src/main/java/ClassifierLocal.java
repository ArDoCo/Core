//import docker.ContainerResponse;
//import docker.DockerManager;

import edu.kit.kastel.informalin.framework.docker.ContainerResponse;
import edu.kit.kastel.informalin.framework.docker.DockerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import records.ClassificationResponse;
import records.ClassifierStatus;

import java.util.Map;

public class ClassifierLocal implements TextClassifier {

    private ContainerResponse container;
    private DockerManager dockerManager;
    private String dockerImageName;
    private final TextClassifier classifier;

    private static final Logger logger = LoggerFactory.getLogger(ClassifierLocal.class);

    public ClassifierLocal(String dockerImageName){
        init(dockerImageName);
        startContainer(-1);
        this.classifier =  new ClassifierNetworkAsync(new AsyncRestAPI("http://127.0.0.1", container.apiPort()));
    }

    public ClassifierLocal(String dockerImageName, int apiPort){
        init(dockerImageName);
        startContainer(apiPort);
        this.classifier =  new ClassifierNetworkAsync(new AsyncRestAPI("http://127.0.0.1", container.apiPort()));
    }

    private void init(String dockerImageName){
        if(dockerImageName.contains(":")){
            this.dockerImageName = dockerImageName.split(":")[0];
            logger.warn("image tags will be ignored");

        } else {
            this.dockerImageName = dockerImageName;
        }
        this.dockerManager = new DockerManager(this.dockerImageName);
    }

    private void startContainer(int apiPort){
        for(String id: dockerManager.getContainerIds()) {
            dockerManager.shutdown(id);
        }

        if(apiPort <= 0){
            this.container = dockerManager.createContainerByImage(dockerImageName, true, true);
        } else {
            this.container = dockerManager.createContainerByImage(dockerImageName, apiPort,true, true);
        }
        logger.info(" successfully started container: " + container.toString());

    }

    @Override
    public ClassificationResponse classifyPhrases(Map<Integer, String> phrases) {
        return classifier.classifyPhrases(phrases);
    }

    @Override
    public ClassifierStatus getClassifierStatus(){
        return classifier.getClassifierStatus();
    }
}
