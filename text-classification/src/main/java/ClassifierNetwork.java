import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ClassifierNetwork implements TextClassifier {

    private static final Logger logger = LoggerFactory.getLogger(ClassifierNetwork.class);
    private RestAPI classificationAPI;

    public ClassifierNetwork(RestAPI classificationAPI) {
        this.classificationAPI = classificationAPI;
    }

    @Override
    public ClassifierStatus getClassifierStatus(){

        JSONObject js = classificationAPI.sendApiRequest("/status");

        if((js.get("status")).equals("ready")){
            return new ClassifierStatus(true);
        }
        return new ClassifierStatus(false);
    }

    @Override
    public ClassificationResponse classifyPhrases(Map<Integer, String> phrases){
       JSONObject response = classificationAPI.sendApiRequest("/classify", new JSONObject(phrases));

       try {
            HashMap<Integer,String> result = new ObjectMapper().readValue(response.toJSONString(), HashMap.class);
            return new ClassificationResponse(result);
        } catch (IOException e) {
           logger.error("Failed to parse json response: " + e.getMessage(), e);
        }

        return null;
    }

}
