package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception;

public class BabelNetInvalidKeyException extends BabelNetException {

    public BabelNetInvalidKeyException(String response) {
        super("The response suggests that an invalid babelnet key was used: " + response);
    }

}
