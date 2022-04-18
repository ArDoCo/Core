package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception;

public class BabelNetRequestLimitException extends BabelNetException {

    public BabelNetRequestLimitException(String response) {
        super("Response suggests that the babelnet request limit was reached: " + response);
    }

}
