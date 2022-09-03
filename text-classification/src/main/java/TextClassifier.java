import records.ClassificationResponse;
import records.ClassifierStatus;

import java.util.Map;
import java.util.concurrent.TimeoutException;

public interface TextClassifier {

    public ClassificationResponse classifyPhrases(Map<Integer, String> phrases) throws TimeoutException;
    public ClassifierStatus getClassifierStatus() throws TimeoutException;

}
