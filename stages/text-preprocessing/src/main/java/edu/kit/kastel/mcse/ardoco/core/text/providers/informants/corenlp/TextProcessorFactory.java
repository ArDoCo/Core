package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp;

import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.config.ConfigManager;

public class TextProcessorFactory {

    public TextProcessor createCoreNlpTextProcessor() {
        if (ConfigManager.getInstance().getProperty("nlpProviderSource").equals("microservice")
                && TextProcessorService.isMicroserviceAvailable()) {
            // return a text processor that uses the CoreNLP microservice
            return new TextProcessorService();
        } else {
            return new TextProcessorLocal();
        }
    }

}
