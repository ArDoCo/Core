package edu.kit.kastel.mcse.ardoco.core.textclassification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.kit.kastel.mcse.ardoco.core.textclassification.records.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClassifierNetworkTest {

    private static final Logger logger = LoggerFactory.getLogger(ClassifierNetworkAsync.class);

    private AsyncRestAPI mockedRestApi;
    private TextClassifier classifier;
    private ScheduledExecutorService scheduler;

    private ObjectMapper mapper;
    @BeforeEach
    private void init(){
        this.mockedRestApi = Mockito.mock(AsyncRestAPI.class);
        this.classifier = new ClassifierNetworkAsync(mockedRestApi, 1000);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.mapper = new ObjectMapper();
    }

    private Future<JsonNode> futureFromJSONObject(JsonNode obj, int time){
        return this. scheduler.schedule(() -> obj, time, TimeUnit.MILLISECONDS);
    }

    private void mockApiStatusResponse(Boolean classifierReady, int time){
        JsonNode jsonStatusResponse;
        try {
            if(classifierReady) {
                jsonStatusResponse = mapper.readTree("{\"status\":\"ready\"}");
            } else {
                jsonStatusResponse = mapper.readTree("{\"status\":\"not-ready\"}");
            }

            when(mockedRestApi.sendApiRequest("/status"))
                    .thenReturn(futureFromJSONObject(jsonStatusResponse, time));

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse json: " + e.getMessage(), e);
        }
    }
    private void mockApiClassificationResponse(JsonNode classificationResponse, int time){
        when(mockedRestApi.sendApiRequest("/classify", classificationResponse))
                .thenReturn(futureFromJSONObject(classificationResponse, time));
    }

    @ParameterizedTest
    @CsvSource({"true,0", "false,250", "true,500", "false,900"})
    @DisplayName("getClassifierStatus-test: wait for response and map status response to ClassifierStatus")
    void getClassifierStatus_statusContainsReady_waitForResponseAndReturnReady(
            boolean ready, int time) throws TimeoutException {
        mockApiStatusResponse(ready, time);
        Assertions.assertEquals(ready, classifier.getClassifierStatus().ready());
    }

    @ParameterizedTest
    @ValueSource(ints = {0,500,900})
    @DisplayName("classifyPhrases-test: wait for response and map response to ClassificationResponse")
    void classifyPhrases_delayedClassificationResponse_waitForResponseAndMapResponse(
            int time) throws TimeoutException {

        Map<Integer, String> testRequest = new HashMap<>() {{
            put(1, "test-phrase-1");
            put(5, "test-phrase-5");
            put(7, "test-phrase-7");
        }};

        JsonNode jsonResponse = mapper.convertValue(testRequest, JsonNode.class);
        mockApiClassificationResponse(jsonResponse, time);
        ClassificationResponse response = classifier.classifyPhrases(testRequest);
        Assertions.assertEquals(response.classifications().size(), testRequest.size());
    }

    @Test
    @DisplayName("getClassifierStatus-test: throw timeout exception if response time exceeds the timeout")
    void getClassifierStatus_takesLongerThanTimeout_throwException(){
        mockApiStatusResponse(true, 2000);
        assertThrows(TimeoutException.class, () -> classifier.getClassifierStatus().ready());
    }

    @Test
    @DisplayName("classifyPhrases-test: throw timeout exception if response time exceeds the timeout")
    void classifyPhrases_takesLongerThanTimeout_throwException(){
        JsonNode jsonResponse = mapper.convertValue(new HashMap<Integer, String>(), JsonNode.class);
        mockApiClassificationResponse(jsonResponse, 2000);
        assertThrows(TimeoutException.class, () -> classifier.classifyPhrases(new HashMap<>()));
    }
}
