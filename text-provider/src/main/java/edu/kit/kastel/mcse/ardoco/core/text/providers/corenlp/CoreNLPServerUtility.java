package edu.kit.kastel.mcse.ardoco.core.text.providers.corenlp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import edu.stanford.nlp.pipeline.StanfordCoreNLPServer;

public class CoreNLPServerUtility {
    private static final Logger logger = LoggerFactory.getLogger(CoreNLPProvider.class);

    protected static int serverPort = 9000;
    protected static String host = "http://localhost";

    private CoreNLPServerUtility() {
    }

    public synchronized static boolean startServer() {
        boolean isAlive = isAlive();
        if (!isAlive) {
            logger.info("Starting server!");
            var serverArguments = createServerArgs();
            try {
                StanfordCoreNLPServer.launchServer(serverArguments);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            isAlive = isAlive();
        }
        logger.info("Server is alive: {}.", isAlive);
        return isAlive;
    }

    private static String[] createServerArgs() {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("corenlp-properties", null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Properties props = CoreNLPProvider.getStanfordProperties(new Properties());
        for (var propertyName : props.stringPropertyNames()) {
            var value = props.getProperty(propertyName);
            if (value != null) {
                var propertyEntry = propertyName + " = " + value + "\n";
                try {
                    Files.writeString(tempFile, propertyEntry, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return new String[] {//
                "-port", Integer.toString(serverPort), //
                "-ssl", Boolean.toString(false), //
                "-annotators", CoreNLPProvider.ANNOTATORS, //
                "-preload", CoreNLPProvider.ANNOTATORS, //
                "-quiet", Boolean.toString(true), //
                "-serverProperties", tempFile.toString(), //
                "-outputFormat", "serialized", //
                "-serializer", "edu.stanford.nlp.pipeline.GenericAnnotationSerializer", "-outputSerializer",
                "edu.stanford.nlp.pipeline.GenericAnnotationSerializer"//
        };
    }

    private static boolean isAlive() {
        var client = new StanfordCoreNLPClient(new Properties(), host, serverPort, 1);
        URL serverUrl = null;
        try {
            serverUrl = new URL(host + ":" + serverPort);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        int counter = 0;
        int limit = 20;
        boolean check = false;
        while (counter < limit && !check) {
            try {
                check = client.checkStatus(serverUrl);
            } catch (RuntimeException e) {
                return false;
            }
            counter++;
            if (!check) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return check;
    }
}
