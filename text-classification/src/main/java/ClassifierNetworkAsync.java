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

    private static final Logger logger = LoggerFactory.getLogger(ClassifierNetwork.class);
    private WebAPI<Future<JSONObject>, JSONObject> classificationAPI;

    public ClassifierNetworkAsync(WebAPI<Future<JSONObject>, JSONObject> classificationAPI) {
        this.classificationAPI = classificationAPI;
    }

    @Override
    public ClassifierStatus getClassifierStatus(){

        Future<JSONObject> futureResponse = classificationAPI.sendApiRequest("/status");

        JSONObject response = null;
        try {
            response = (JSONObject) futureResponse.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

        if((response.get("status")).equals("ready")){
            return new ClassifierStatus(true);
        }
        return new ClassifierStatus(false);
    }

    @Override
    public ClassificationResponse classifyPhrases(Map<Integer, String> phrases){
        Future futureResponse = classificationAPI.sendApiRequest("/classify", new JSONObject(phrases));

        JSONObject response = null;
        try {
            response = (JSONObject) futureResponse.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }

        try {
            HashMap<Integer,String> result = new ObjectMapper().readValue(response.toJSONString(), HashMap.class);
            return new ClassificationResponse(result);
        } catch (IOException e) {
            logger.error("Failed to parse json response: " + e.getMessage(), e);
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
