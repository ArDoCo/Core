/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.informalin.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.ConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.ConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationMetrics;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.EvaluationResultsImpl;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files.TLGoldStandardFile;

/**
 * Represents the trace link evaluation result for a single project.
 */
public class TLProjectEvalResult extends EvaluationResultsImpl implements Comparable<TLProjectEvalResult> {

    private final Project project;
    private final double precision, recall, f1Score;

    // Links:
    private final List<TestLink> foundLinks = new ArrayList<>();
    private final List<TestLink> correctLinks = new ArrayList<>();
    private final List<TestLink> truePositives = new ArrayList<>();
    private final List<TestLink> falsePositives = new ArrayList<>();
    private final List<TestLink> falseNegatives = new ArrayList<>();

    public TLProjectEvalResult(Project project, DataRepository data) throws IOException {
        this(project, getTraceLinks(data), TLGoldStandardFile.loadLinks(project));
    }

    private static List<TestLink> getTraceLinks(DataRepository data) {
        var traceLinks = Lists.mutable.<TestLink>empty();
        var connectionStates = data.getData(ConnectionStates.ID, ConnectionStates.class).orElseThrow();
        var modelStates = data.getData(ModelStates.ID, ModelStates.class).orElseThrow();

        List<ConnectionState> connectionStatesList = modelStates.modelIds()
                .stream()
                .map(modelStates::getModelState)
                .map(ModelExtractionState::getMetamodel)
                .map(connectionStates::getConnectionState)
                .toList();
        for (var connectionState : connectionStatesList) {
            traceLinks.addAll(connectionState.getTraceLinks().stream().map(TestLink::new).toList());
        }
        return traceLinks.toList();
    }

    public TLProjectEvalResult(Project project, Collection<TestLink> foundLinks, Collection<TestLink> correctLinks) {
        this.project = project;
        this.foundLinks.addAll(foundLinks);
        this.correctLinks.addAll(correctLinks);

        Set<TestLink> distinctTraceLinks = new HashSet<>(this.foundLinks);
        Set<TestLink> distinctGoldStandard = new HashSet<>(this.correctLinks);

        // True Positives are the trace links that are contained on both lists
        Set<TestLink> truePositives = distinctTraceLinks.stream().filter(distinctGoldStandard::contains).collect(Collectors.toSet());
        int tp = truePositives.size();

        // False Positives are the trace links that are only contained in the result set
        Set<TestLink> falsePositives = distinctTraceLinks.stream().filter(tl -> !distinctGoldStandard.contains(tl)).collect(Collectors.toSet());
        int fp = falsePositives.size();

        // False Negatives are the trace links that are only contained in the gold standard
        Set<TestLink> falseNegatives = distinctGoldStandard.stream().filter(tl -> !distinctTraceLinks.contains(tl)).collect(Collectors.toSet());
        int fn = falseNegatives.size();

        this.precision = EvaluationMetrics.calculatePrecision(tp, fp);
        this.recall = EvaluationMetrics.calculateRecall(tp, fn);
        this.f1Score = EvaluationMetrics.calculateF1(precision, recall);

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

    @Override
    public double getPrecision() {
        return precision;
    }

    @Override
    public double getRecall() {
        return recall;
    }

    @Override
    public double getF1() {
        return f1Score;
    }

    public List<TestLink> getFoundLinks() {
        return foundLinks;
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
