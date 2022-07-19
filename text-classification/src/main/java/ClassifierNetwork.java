import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class ClassifierNetwork implements IClassifier{

    private static final Logger logger = LoggerFactory.getLogger(ClassifierNetwork.class);
    private String url;
    private int port;

    public ClassifierNetwork(String url, int port) {
        this.url = url;
        this.port = port;
    }

    private JSONObject sendApiRequest(String endpoint, JSONObject requestData) {
        HttpURLConnection connection;
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();

        String ep = endpoint;
        if(!endpoint.startsWith("/")){
            ep = "/"+endpoint;
        }

        try{
            URL url = new URL(this.url + ":" + String.valueOf(port) + ep);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            String jsonInputString = requestData.toJSONString();

            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int status = connection.getResponseCode();

            if (status >= 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            }
            else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            }

            try {
                Object ob = new JSONParser().parse(responseContent.toString());
                JSONObject js = (JSONObject) ob;

                return js;

            } catch (ParseException e) {
                logger.error("Failed to parse json response: " + e.getMessage(), e);
            }
            connection.disconnect();
        }
        catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private JSONObject sendApiRequest(String endpoint){
        HttpURLConnection connection;
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();

        String ep = endpoint;
        if(!endpoint.startsWith("/")){
            ep = "/"+endpoint;
        }

        try{
            URL url = new URL(this.url + ":" + String.valueOf(port) + ep);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();

            if (status >= 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            }
            else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line);
                }
                reader.close();
            }

            try {
                Object ob = new JSONParser().parse(responseContent.toString());
                JSONObject js = (JSONObject) ob;

                return js;

            } catch (ParseException e) {
                logger.error("Failed to parse json response: " + e.getMessage(), e);
            }
            connection.disconnect();
        }
        catch (MalformedURLException e) {
            logger.error("Failed to request status: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Failed to request status: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ClassifierStatus getClassifierStatus(){

        JSONObject js = sendApiRequest("/status");

        if((js.get("status")).equals("ready")){
            return new ClassifierStatus(true);
        }
        return new ClassifierStatus(false);
    }

    @Override
    public ClassificationResponse classifyPhrases(Map<Integer, String> phrases){
       JSONObject response = sendApiRequest("/classify", new JSONObject(phrases));

       try {
            HashMap<Integer,String> result = new ObjectMapper().readValue(response.toJSONString(), HashMap.class);
            return new ClassificationResponse(result);
        } catch (IOException e) {
           logger.error("Failed to parse json response: " + e.getMessage(), e);
        }

        return null;
    }

}
