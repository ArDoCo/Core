/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.config.ConfigManager;
import io.github.ardoco.textproviderjson.error.InvalidJsonException;
import io.github.ardoco.textproviderjson.error.NotConvertableException;

/**
 * This text processor processes texts using CoreNLP.
 */
public class TextProcessor {

    private static final int MAX_FAILED_SERVICE_REQUESTS = 2;
    Logger logger = LoggerFactory.getLogger(TextProcessor.class);

    /**
     * processes and annotates a given text
     * 
     * @param inputText the input text
     * @return the annotated text
     */
    public Text processText(String inputText) {
        boolean microserviceAvailable;
        try {
            microserviceAvailable = MicroserviceChecker.isMicroserviceAvailable();
        } catch (IOException e) {
            microserviceAvailable = false;
            logger.warn("Could not check if CoreNLP microservice is available. ", e);
        }
        if (ConfigManager.getInstance().getNlpProviderSource().equals("microservice") && microserviceAvailable) {
            int k = 0;
            while (k < MAX_FAILED_SERVICE_REQUESTS) {
                try {
                    Text processedText = processService(inputText);
                    logger.info("Processed text with CoreNLP microservice.");
                    return processedText;
                } catch (IOException e) {
                    k++;
                    logger.warn("Could not process text with CoreNLP microservice. Trying again. ", e);
                } catch (NotConvertableException | InvalidJsonException e) {
                    logger.warn("Could not process text with CoreNLP microservice. Text not convertable. ", e);
                    return processLocally(inputText);
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

    private Text processService(String inputText) throws IOException, NotConvertableException, InvalidJsonException {
        return new TextProcessorService().processText(inputText);
    }

}
