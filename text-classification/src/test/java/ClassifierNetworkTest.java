import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import records.ClassificationResponse;

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
    @BeforeEach
    private void init(){
        this.mockedRestApi = Mockito.mock(AsyncRestAPI.class);
        this.classifier = new ClassifierNetworkAsync(mockedRestApi, 1000);
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    private Future<JSONObject> futureFromJSONObject(JSONObject obj, int time){
        return this. scheduler.schedule(() -> obj, time, TimeUnit.MILLISECONDS);
    }

    private void mockApiStatusResponse(Boolean classifierReady, int time){
        JSONObject jsonStatusResponse = null;
        try {
            if(classifierReady) {
                jsonStatusResponse = (JSONObject) new JSONParser().parse("{\"status\":\"ready\"}");
            } else {
                jsonStatusResponse = (JSONObject) new JSONParser().parse("{\"status\":\"notready\"}");
            }

            when(mockedRestApi.sendApiRequest("/status"))
                    .thenReturn(futureFromJSONObject(jsonStatusResponse, time));

        } catch (ParseException e) {
            logger.error("Failed to parse json: " + e.getMessage(), e);
        }
    }

    private void mockApiClassificationResponse(JSONObject classificationResponse, int time){
        when(mockedRestApi.sendApiRequest("/classify", classificationResponse))
                .thenReturn(futureFromJSONObject(classificationResponse, time));
    }

    @ParameterizedTest
    @CsvSource({"true,0", "false,250", "true,500", "false, 900"})
    void getClassifierStatus_ifStatusContainsReady_returnReady(boolean ready, int time) throws TimeoutException {

        mockApiStatusResponse(ready, time);
        Assertions.assertEquals(classifier.getClassifierStatus().ready(), ready);

    }

    @ParameterizedTest
    @ValueSource(ints = {0,500,900})
    void classifyPhrases_jsonResponse_parseResponse(int time) throws TimeoutException {

        Map<Integer, String> testRequest = new HashMap<>() {{
            put(1, "test-phrase-1");
            put(5, "test-phrase-5");
            put(7, "test-phrase-7");
        }};

        mockApiClassificationResponse(new JSONObject(testRequest), time);
        ClassificationResponse response = classifier.classifyPhrases(new JSONObject(testRequest));
        Assertions.assertEquals(response.classifications().size(), testRequest.size());
    }

    @Test
    void getClassifierStatus_takesLongerThanTimeout_throwException(){
        mockApiStatusResponse(true, 2000);
        Exception exception = assertThrows(TimeoutException.class, () -> {
            classifier.getClassifierStatus().ready();
        });
    }

    @Test
    void classifyPhrases_takesLongerThanTimeout_throwException(){
        mockApiClassificationResponse(new JSONObject(new HashMap<Integer, String>()), 2000);
        Exception exception = assertThrows(TimeoutException.class, () -> {
            ClassificationResponse response =
                    classifier.classifyPhrases(new JSONObject(new HashMap<Integer, String>()));
        });
    }
}
