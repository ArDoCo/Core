package edu.kit.kastel.mcse.ardoco.core.textclassification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncRestAPI implements WebAPI<Future<JsonNode>, JsonNode>{
    private static final Logger logger = LoggerFactory.getLogger(AsyncRestAPI.class);
    private final String url;
    private final int port;
    private final ExecutorService executor;
    private final ObjectMapper mapper;

    public AsyncRestAPI(String url, int port) {
        this.url = url;
        this.port = port;
        this.executor= Executors.newSingleThreadExecutor();
        this.mapper = new ObjectMapper();
    }

    private JsonNode parseJsonString(String jsonString){
        JsonNode obj = null;
        try {
            obj = this.mapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            logger.error("Failed to parse json string" + e.getMessage(), e);
        }
        return  obj;
    }

    private HttpURLConnection setUpConnection(String endpoint) throws IOException {

        String ep = endpoint;
        if(!endpoint.startsWith("/")){
            ep = "/"+endpoint;
        }

        URL u = new URL(this.url + ":" + port + ep);
        HttpURLConnection connection = (HttpURLConnection) u.openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        return connection;
    }

    private JsonNode receiveRequestResponse(HttpURLConnection connection) throws IOException {
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();

        int status = connection.getResponseCode();

        if (status >= 300) {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }
        else {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        }

        while ((line = reader.readLine()) != null) {
            responseContent.append(line);
        }
        reader.close();

        connection.disconnect();
        return parseJsonString(responseContent.toString());
    }

    @Override
    public Future<JsonNode> sendApiRequest(String endpoint, JsonNode requestData) {
        return executor.submit(() -> {
            try{
                HttpURLConnection connection = setUpConnection(endpoint);
                String jsonInputString = mapper.writeValueAsString(requestData);

                try(OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                return receiveRequestResponse(connection);

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
            return null;
        });
    }

    @Override
    public Future<JsonNode> sendApiRequest(String endpoint) {
        return executor.submit(() -> {
            try{
                HttpURLConnection connection = setUpConnection(endpoint);
                return receiveRequestResponse(connection);

            } catch (IOException e) {
                logger.error("Failed to request status: " + e.getMessage(), e);
            }
            return null;
        });
    }
}