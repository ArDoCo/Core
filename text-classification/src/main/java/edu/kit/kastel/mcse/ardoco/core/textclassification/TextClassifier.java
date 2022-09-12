package edu.kit.kastel.mcse.ardoco.core.textclassification;

import edu.kit.kastel.mcse.ardoco.core.textclassification.records.*;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * The TextClassifier interface defines methods for classifying text phrases
 */
public interface TextClassifier {
    /**
     * classifies all phrases given in a map
     * @param phrases a map with the phrases to be classified. The keys are identifiers for the phrases.
     * @return the classified phrases
     * @throws TimeoutException in case the classification process takes too long
     */
    ClassificationResponse classifyPhrases(Map<Integer, String> phrases) throws TimeoutException;

    /**
     * returns the classifiers' status i.e. if the classifier is ready
     * @return the status
     * @throws TimeoutException in case the status response takes too long
     */
    ClassifierStatus getClassifierStatus() throws TimeoutException;

}
