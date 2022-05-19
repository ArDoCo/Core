package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A eval result represents the result of an entire evaluation.
 * It contains the results of each evaluated project.
 */
public class EvalResult {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(TestLink.class, new TestLinkSerialization())
            .create();

    public static EvalResult fromJsonString(String jsonStr) {
        return GSON.fromJson(jsonStr, EvalResult.class);
    }

    private final double precision, recall, f1Score, accuracy;
    private final int sentenceCount, modelInstanceCount, truePositiveCount, trueNegativeCount, falsePositiveCount, falseNegativeCount;

    private final List<EvalProjectResult> projectResults = new ArrayList<>();

    public EvalResult(List<EvalProjectResult> projectResults) {
        this.projectResults.addAll(projectResults);
        this.projectResults.sort(Comparator.comparing(res -> res.getProject().name()));

        this.sentenceCount = projectResults.stream().mapToInt(EvalProjectResult::getSentenceCount).sum();
        this.modelInstanceCount = projectResults.stream().mapToInt(EvalProjectResult::getModelInstanceCount).sum();
        this.truePositiveCount = projectResults.stream().mapToInt(res -> res.getTruePositives().size()).sum();
        this.trueNegativeCount = projectResults.stream().mapToInt(EvalProjectResult::getTrueNegativeCount).sum();
        this.falsePositiveCount = projectResults.stream().mapToInt(res -> res.getFalsePositives().size()).sum();
        this.falseNegativeCount = projectResults.stream().mapToInt(res -> res.getFalseNegatives().size()).sum();

        double tp = this.truePositiveCount;
        double tn = this.trueNegativeCount;
        double fp = this.falsePositiveCount;
        double fn = this.falseNegativeCount;

        this.precision = tp / (tp + fp);
        this.recall = tp / (tp + fn);
        this.f1Score = 2 * precision * recall / (precision + recall);
        this.accuracy = (tp + tn) / (tp + tn + fp + fn);
    }

    public List<EvalProjectResult> getProjectResults() {
        return projectResults;
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getF1Score() {
        return f1Score;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public List<TestLink> getFoundLinks() {
        return projectResults.stream().flatMap(res -> res.getFoundLinks().stream()).toList();
    }

    public List<TestLink> getCorrectLinks() {
        return projectResults.stream().flatMap(res -> res.getCorrectLinks().stream()).toList();
    }

    public List<TestLink> getTruePositives() {
        return projectResults.stream().flatMap(res -> res.getTruePositives().stream()).toList();
    }

    public List<TestLink> getFalsePositives() {
        return projectResults.stream().flatMap(res -> res.getFalsePositives().stream()).toList();
    }

    public List<TestLink> getFalseNegatives() {
        return projectResults.stream().flatMap(res -> res.getFalseNegatives().stream()).toList();
    }

    public int getSentenceCount() {
        return sentenceCount;
    }

    public int getModelInstanceCount() {
        return modelInstanceCount;
    }

    public int getTruePositiveCount() {
        return truePositiveCount;
    }

    public int getTrueNegativeCount() {
        return trueNegativeCount;
    }

    public int getFalsePositiveCount() {
        return falsePositiveCount;
    }

    public int getFalseNegativeCount() {
        return falseNegativeCount;
    }

    public String toJsonString() {
        return GSON.toJson(this);
    }

}
