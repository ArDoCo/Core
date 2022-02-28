/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.common.AgentDatastructure;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.inconsistencies.eval.EvaluationResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files.TLGoldStandardFile;

/**
 * Represents the trace link evaluation result for a single project.
 */
public class TLProjectEvalResult implements Comparable<TLProjectEvalResult>, EvaluationResult {

    private final Project project;
    private final double precision, recall, f1Score;

    // Links:
    private final List<TestLink> foundLinks = new ArrayList<>();
    private final List<TestLink> correctLinks = new ArrayList<>();
    private final List<TestLink> truePositives = new ArrayList<>();
    private final List<TestLink> falsePositives = new ArrayList<>();
    private final List<TestLink> falseNegatives = new ArrayList<>();

    public TLProjectEvalResult(Project project, AgentDatastructure data) throws IOException {
        this(project, data.getConnectionState().getTraceLinks().stream().map(TestLink::new).toList(), TLGoldStandardFile.loadLinks(project));
    }

    public TLProjectEvalResult(Project project, Collection<TestLink> foundLinks, Collection<TestLink> correctLinks) {
        this.project = project;
        this.foundLinks.addAll(foundLinks);
        this.correctLinks.addAll(correctLinks);

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

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getF1() {
        return f1Score;
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

    @Override
    public int compareTo(TLProjectEvalResult o) {
        return this.project.name().compareTo(o.project.name());
    }

}
