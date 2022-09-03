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

    private final int timeout;

    public ClassifierNetworkAsync(WebAPI<Future<JSONObject>, JSONObject> classificationAPI, int timeout) {
        this.classificationAPI = classificationAPI;
        this.timeout = timeout;
    }

    @Override
    public ClassifierStatus getClassifierStatus() throws TimeoutException {

        Future<JSONObject> futureResponse = classificationAPI.sendApiRequest("/status");
        JSONObject response = null;

        try {
            response = futureResponse.get(this.timeout, TimeUnit.MILLISECONDS);
            if((response.get("status")).equals("ready")){
                return new ClassifierStatus(true);
            }
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException ie) {
            logger.error("InterruptedException: ", ie);
            Thread.currentThread().interrupt();
        } catch (TimeoutException te) {
        logger.error(te.getMessage(), te);
        throw new TimeoutException("The result was not ready after" + this.timeout + "miliseconds.");
    }

        return new ClassifierStatus(false);
    }

    @Override
    public ClassificationResponse classifyPhrases(Map<Integer, String> phrases) throws TimeoutException {
        Future<JSONObject> futureResponse = classificationAPI.sendApiRequest("/classify", new JSONObject(phrases));
        JSONObject response = null;

        try {
            response = futureResponse.get(this.timeout, TimeUnit.MILLISECONDS);
            HashMap<Integer,String> result = new ObjectMapper().readValue(response.toJSONString(), HashMap.class);
            return new ClassificationResponse(result);
        } catch (ExecutionException | IOException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException ie) {
            logger.error("InterruptedException: ", ie);
            Thread.currentThread().interrupt();
        } catch (TimeoutException te) {
            logger.error(te.getMessage(), te);
            throw new TimeoutException("The result was not ready after" + this.timeout + "miliseconds.");
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
