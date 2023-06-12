package edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp;

public class TextProcessorFactory {

    private String coreNlpProviderSrc = "Service"; // todo: config

    public TextProcessor createCoreNlpTextProcessor() {
        if (coreNlpProviderSrc.equals("Service")) {
            return new TextProcessorService();
        } else {
            return new TextProcessorLocal();
        }
    }

}
