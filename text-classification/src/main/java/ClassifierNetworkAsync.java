import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import records.ClassificationResponse;
import records.ClassifierStatus;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class ClassifierNetworkAsync implements TextClassifier {

    private static final Logger logger = LoggerFactory.getLogger(ClassifierNetworkAsync.class);
    private final WebAPI<Future<JSONObject>, JSONObject> classificationAPI;

    public ClassifierNetworkAsync(WebAPI<Future<JSONObject>, JSONObject> classificationAPI) {
        this.classificationAPI = classificationAPI;
    }

    @Override
    public ClassifierStatus getClassifierStatus(){

        Future<JSONObject> futureResponse = classificationAPI.sendApiRequest("/status");
        JSONObject response = null;

        try {
            response = futureResponse.get(1000, TimeUnit.MILLISECONDS);
            if((response.get("status")).equals("ready")){
                return new ClassifierStatus(true);
            }
        } catch (ExecutionException | TimeoutException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException ie) {
            logger.error("InterruptedException: ", ie);
            Thread.currentThread().interrupt();
        }

        return new ClassifierStatus(false);
    }

    @Override
    public ClassificationResponse classifyPhrases(Map<Integer, String> phrases){
        Future<JSONObject> futureResponse = classificationAPI.sendApiRequest("/classify", new JSONObject(phrases));
        JSONObject response = null;

        try {
            response = futureResponse.get(1000, TimeUnit.MILLISECONDS);
            HashMap<Integer,String> result = new ObjectMapper().readValue(response.toJSONString(), HashMap.class);
            return new ClassificationResponse(result);
        } catch (ExecutionException | TimeoutException | IOException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException ie) {
            logger.error("InterruptedException: ", ie);
            Thread.currentThread().interrupt();
        }

        return null;
    }
    public Future<ClassificationResponse> classifyPhrasesAsync(Map<Integer, String> phrases){
        ExecutorService executor= Executors.newSingleThreadExecutor();
        return executor.submit(() -> this.classifyPhrases(phrases));
    }

    public Future<ClassifierStatus> getClassifierStatusAsync(){
        ExecutorService executor= Executors.newSingleThreadExecutor();
        return executor.submit(() -> this.getClassifierStatus());
    }

}
