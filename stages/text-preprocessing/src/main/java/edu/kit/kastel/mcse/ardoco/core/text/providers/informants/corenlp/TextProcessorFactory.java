package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp;

import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.config.ConfigManager;
import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor.TextProcessor;
import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor.TextProcessorLocal;
import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.textprocessor.TextProcessorService;

public class TextProcessorFactory {

    public TextProcessor createCoreNlpTextProcessor() {
        if (ConfigManager.getInstance().getProperty("nlpProviderSource").equals("microservice")
                && MicroserviceChecker.isMicroserviceAvailable()) {
            // return a text processor that uses the CoreNLP microservice
            return new TextProcessorService();
        } else {
            return new TextProcessorLocal();
        }
    }

}
