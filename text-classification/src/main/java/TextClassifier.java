import java.util.Map;

public interface TextClassifier {

    public ClassificationResponse classifyPhrases(Map<Integer, String> phrases);
    public ClassifierStatus getClassifierStatus();

}
