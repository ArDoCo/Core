package edu.kit.kastel.mcse.ardoco.core.textclassification;

import edu.kit.kastel.mcse.ardoco.core.textclassification.records.*;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public interface TextClassifier {

    ClassificationResponse classifyPhrases(Map<Integer, String> phrases) throws TimeoutException;
    ClassifierStatus getClassifierStatus() throws TimeoutException;

}
