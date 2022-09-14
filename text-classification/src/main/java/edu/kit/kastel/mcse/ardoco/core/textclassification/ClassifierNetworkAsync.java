/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.textclassification;

import java.util.Map;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.kit.kastel.mcse.ardoco.core.textclassification.records.*;

public class ClassifierNetworkAsync implements TextClassifier {

    private static final Logger logger = LoggerFactory.getLogger(ClassifierNetworkAsync.class);
    private final WebAPI<Future<JsonNode>, JsonNode> classificationAPI;
    private final int timeout;
    private final ObjectMapper mapper;

    /**
     * ClassifierNetworkAsync implements a {@link TextClassifier} by connecting over a {@link AsyncRestAPI} to an external classifier
     * deployed as a web service. In addition to the methods defined in the interface, it provides methods that
     * return asynchronous results.
     * 
     * @param classificationAPI the {@link WebAPI}that connects to the external classifier and returns asynchronous results.
     * @param timeout           the maximum time to wait for API responses.
     */
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
            if ((response.get("status")).asText().equals("ready")) {
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

    /**
     * equivalent to {@link #classifyPhrases(Map) classifyPhrases} but asynchronous i.e. do not wait for the result
     * and return a <a href="https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html">Future</a> instead.
     * 
     * @param phrases a map with the phrases to be classified. The keys are identifiers for the phrases.
     * @return future result of the classification
     */
    public Future<ClassificationResponse> classifyPhrasesAsync(Map<Integer, String> phrases) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(() -> this.classifyPhrases(phrases));
    }

    /**
     * equivalent to {@link #getClassifierStatus() getClassiifierStatus} but asynchronous i.e. do not wait for the response
     * and return a <a href="https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/Future.html">Future</a> instead.
     * 
     * @return future response of the classifiers' status request
     */
    public Future<ClassifierStatus> getClassifierStatusAsync() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(() -> this.getClassifierStatus());
    }

}
