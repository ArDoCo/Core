import java.util.Map;

public interface IClassifier {

    public ClassificationResponse classifyPhrases(Map<Integer, String> phrases);
    public ClassifierStatus getClassifierStatus();

}
