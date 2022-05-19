package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.TLGoldStandardFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The evaluation result of a specific project.
 */
public class EvalProjectResult implements Comparable<EvalProjectResult> {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(TestLink.class, new TestLinkSerialization())
            .create();

    public static EvalProjectResult fromJsonString(String jsonStr) {
        return GSON.fromJson(jsonStr, EvalProjectResult.class);
    }

    private final Project project;

    private final List<TestLink> foundLinks = new ArrayList<>();
    private final List<TestLink> correctLinks = new ArrayList<>();
    private final List<TestLink> truePositives = new ArrayList<>();
    private final List<TestLink> falsePositives = new ArrayList<>();
    private final List<TestLink> falseNegatives = new ArrayList<>();

    private final double precision, recall, f1Score, accuracy;
    private final int sentenceCount, modelInstanceCount, truePositiveCount, trueNegativeCount, falsePositiveCount, falseNegativeCount;
    // ^ unused fields exist for json serialization/deserialization

    public EvalProjectResult(Project project, DataStructure data) throws IOException {
        this(
                project,
                EvalUtils.getTraceLinks(data).stream().map(TestLink::new).toList(),
                TLGoldStandardFile.loadLinks(project),
                data.getText().getSentences().size(),
                EvalUtils.getInstances(data).size()
        );
    }

    public EvalProjectResult(Project project, List<TestLink> foundLinks, List<TestLink> correctLinks, int sentenceCount, int modelInstanceCount) {
        this.project = Objects.requireNonNull(project);
        this.foundLinks.addAll(foundLinks);
        this.correctLinks.addAll(correctLinks);
        this.sentenceCount = sentenceCount;
        this.modelInstanceCount = modelInstanceCount;

        // --- copied from TestUtil#compare but with TestLink instead of strings ---
        Set<TestLink> distinctTraceLinks = new HashSet<>(this.foundLinks);
        Set<TestLink> distinctGoldStandard = new HashSet<>(this.correctLinks);

        // True Positives are the trace links that are contained on both lists
        Set<TestLink> truePositives = distinctTraceLinks.stream().filter(distinctGoldStandard::contains).collect(Collectors.toSet());
        double tp = truePositives.size();

        // False Positives are the trace links that are only contained in the result set
        Set<TestLink> falsePositives = distinctTraceLinks.stream().filter(tl -> !distinctGoldStandard.contains(tl)).collect(Collectors.toSet());
        double fp = falsePositives.size();

        // False Negatives are the trace links that are only contained in the gold standard
        Set<TestLink> falseNegatives = distinctGoldStandard.stream().filter(tl -> !distinctTraceLinks.contains(tl)).collect(Collectors.toSet());
        double fn = falseNegatives.size();

        this.precision = tp / (tp + fp);
        this.recall = tp / (tp + fn);
        this.f1Score = 2 * precision * recall / (precision + recall);
        // --- end of copy ---

        this.truePositives.addAll(truePositives);
        this.falsePositives.addAll(falsePositives);
        this.falseNegatives.addAll(falseNegatives);

        this.truePositiveCount = this.truePositives.size();
        this.trueNegativeCount = (sentenceCount * modelInstanceCount) - truePositives.size() - falsePositives.size() - falseNegatives.size();
        this.falsePositiveCount = this.falsePositives.size();
        this.falseNegativeCount = this.falseNegatives.size();

        double tn = this.trueNegativeCount;
        this.accuracy = (tp + tn) / (tp + tn + fp + fn);

        Collections.sort(this.foundLinks);
        Collections.sort(this.correctLinks);
        Collections.sort(this.truePositives);
        Collections.sort(this.falsePositives);
        Collections.sort(this.falseNegatives);
	    // ^ sorting these lists ensures that they are always in the same order when viewed
    }

    public Project getProject() {
        return project;
    }

    public List<TestLink> getFoundLinks() {
        return foundLinks;
    }

    public List<TestLink> getCorrectLinks() {
        return correctLinks;
    }

    public List<TestLink> getTruePositives() {
        return truePositives;
    }

    public List<TestLink> getFalsePositives() {
        return falsePositives;
    }

    public List<TestLink> getFalseNegatives() {
        return falseNegatives;
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

    public int getSentenceCount() {
        return sentenceCount;
    }

    public int getModelInstanceCount() {
        return modelInstanceCount;
    }

    public int getTrueNegativeCount() {
        return trueNegativeCount;
    }

    @Override public int compareTo(@NotNull EvalProjectResult o) {
        return this.project.name().compareTo(o.project.name());
    }

    public String toJsonString() {
        return GSON.toJson(this);
    }

}
