package edu.kit.kastel.mcse.ardoco.core.textclassification;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import edu.kit.kastel.mcse.ardoco.core.textclassification.records.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

public class ClassifierNetworkAsync implements TextClassifier {

    private static final Logger logger = LoggerFactory.getLogger(ClassifierNetworkAsync.class);
    private final WebAPI<Future<JsonNode>, JsonNode> classificationAPI;
    private final int timeout;
    private final ObjectMapper mapper;

    public ClassifierNetworkAsync(WebAPI<Future<JsonNode>, JsonNode> classificationAPI, int timeout) {
        this.classificationAPI = classificationAPI;
        this.timeout = timeout;
        this.mapper = new ObjectMapper();
    }

    @Override
    public ClassifierStatus getClassifierStatus() throws TimeoutException {

        Future<JsonNode> futureResponse = classificationAPI.sendApiRequest("/status");
        try {
            JsonNode response = futureResponse.get(this.timeout, TimeUnit.MILLISECONDS);
            if((response.get("status")).asText().equals("ready")){
                return new ClassifierStatus(true);
            }
        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException ie) {
            logger.error("InterruptedException: ", ie);
            Thread.currentThread().interrupt();
        } catch (TimeoutException te) {
        logger.error(te.getMessage(), te);
        throw new TimeoutException("The result was not ready after" + this.timeout + "milliseconds.");
    }

        return new ClassifierStatus(false);
    }

    @Override
    public ClassificationResponse classifyPhrases(Map<Integer, String> phrases) throws TimeoutException {
        JsonNode jsonRequest = mapper.convertValue(phrases, JsonNode.class);
        Future<JsonNode> futureResponse = classificationAPI.sendApiRequest("/classify", jsonRequest);
        try {
            JsonNode response = futureResponse.get(this.timeout, TimeUnit.MILLISECONDS);

            Map<Integer, String> result = mapper.convertValue(response, new TypeReference<>() {
            });
            return new ClassificationResponse(result);

        } catch (ExecutionException e) {
            logger.error(e.getMessage(), e);
        } catch (InterruptedException ie) {
            logger.error("InterruptedException: ", ie);
            Thread.currentThread().interrupt();
        } catch (TimeoutException te) {
            logger.error(te.getMessage(), te);
            throw new TimeoutException("The result was not ready after" + this.timeout + "milliseconds.");
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
