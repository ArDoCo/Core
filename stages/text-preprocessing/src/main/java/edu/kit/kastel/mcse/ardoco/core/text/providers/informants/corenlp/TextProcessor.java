package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.config.ConfigManager;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This text processor processes texts using CoreNLP.
 */
public class TextProcessor {

    private final int maxFailedServiceRequests = 2;
    Logger logger = LoggerFactory.getLogger(TextProcessor.class);

    /**
     * processes and annotates a given text
     * @param inputText the input text
     * @return          the annotated text
     */
    public Text processText(String inputText) {
        if (ConfigManager.getInstance().getProperty("nlpProviderSource").equals("microservice")
                && MicroserviceChecker.isMicroserviceAvailable()) {
            // return a text processor that uses the CoreNLP microservice
            int k = 0;
            while (k < maxFailedServiceRequests) {
                try {
                    Text processedText = processService(inputText);
                    logger.info("Processed text with CoreNLP microservice.");
                    return processedText;
                } catch (IOException e) {
                    k++;
                }
            }
            logger.warn("Could not process text with CoreNLP microservice. Processing locally instead.");
        }
        logger.info("Processed text locally.");
        return processLocally(inputText);
    }

    private Text processLocally(String inputText) {
        return new TextProcessorLocal().processText(inputText);
    }

    private Text processService(String inputText) throws IOException {
        return new TextProcessorService().processText(inputText);
    }

}
