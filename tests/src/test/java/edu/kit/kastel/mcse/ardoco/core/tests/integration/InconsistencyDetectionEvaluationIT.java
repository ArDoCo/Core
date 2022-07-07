/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.types.MissingModelInstanceInconsistency;
import edu.kit.kastel.mcse.ardoco.core.model.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.tests.TestUtil;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ExplicitEvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ResultCalculator;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.inconsistencyhelper.HoldBackRunResultsProducer;

/**
 * Integration test that evaluates the inconsistency detection capabilities of ArDoCo. Runs on the projects that are
 * defined in the enum {@link Project}.
 *
 * Currently, the focus lies on detecting elements that are mentioned in the text but are not represented in the model.
 * For this, we run an evaluation that holds back (removes) one element from the model. This way, we know that there is
 * a missing element and the trace links to this element (in the gold standard) are the spots of inconsistency then. We
 * run this multiple times so each element was held back once.
 *
 * @author Jan Keim
 */
class InconsistencyDetectionEvaluationIT {
    private static final Logger logger = LoggerFactory.getLogger(InconsistencyDetectionEvaluationIT.class);
    private static final String OUTPUT = "src/test/resources/testout";

    /**
     * Tests the inconsistency detection on all {@link Project projects}.
     *
     * @param project Project that gets inserted automatically with the enum {@link Project}.
     */
    @DisplayName("Evaluate Inconsistency Analyses")
    @ParameterizedTest(name = "Evaluating {0}")
    @EnumSource(Project.class)
    void inconsistencyIT(Project project) {
        HoldBackRunResultsProducer holdBackRunResultsProducer = new HoldBackRunResultsProducer();
        Map<ModelInstance, DataStructure> runs = holdBackRunResultsProducer.produceHoldBackRunResults(project, false);

        ResultCalculator resultCalculator = calculateEvaluationResults(project, runs);
        var weightedResults = resultCalculator.getWeightedAveragePRF1();

        EvaluationResults expectedInconsistencyResults = project.getExpectedInconsistencyResults();
        logResults(project, weightedResults, expectedInconsistencyResults);
        checkResults(weightedResults, expectedInconsistencyResults);
    }

    /**
     * Tests the baseline approach that reports an inconsistency for each sentence that is not traced to a model
     * element. This test is enabled by providing the environment variable "testBaseline" with any value.
     *
     * @param project Project that gets inserted automatically with the enum {@link Project}.
     */
    @EnabledIfEnvironmentVariable(named = "testBaseline", matches = ".*")
    @DisplayName("Evaluate Inconsistency Analyses Baseline")
    @ParameterizedTest(name = "Evaluating Baseline For {0}")
    @EnumSource(Project.class)
    void inconsistencyBaselineIT(Project project) {
        HoldBackRunResultsProducer holdBackRunResultsProducer = new HoldBackRunResultsProducer();
        Map<ModelInstance, DataStructure> runs = holdBackRunResultsProducer.produceHoldBackRunResults(project, true);

        Assertions.assertTrue(runs != null && runs.size() > 0);

        ResultCalculator resultCalculator = calculateEvaluationResults(project, runs);
        var weightedResults = resultCalculator.getWeightedAveragePRF1();

        EvaluationResults expectedInconsistencyResults = project.getExpectedInconsistencyResults();
        logResults(project, weightedResults, expectedInconsistencyResults);
    }

    private ResultCalculator calculateEvaluationResults(Project project, Map<ModelInstance, DataStructure> runs) {
        ResultCalculator resultCalculator = new ResultCalculator();
        for (var run : runs.entrySet()) {
            var runEvalResults = evaluateRun(project, run.getKey(), run.getValue());
            if (runEvalResults != null) {
                int fn = runEvalResults.getFalseNegative().size();
                int fp = runEvalResults.getFalsePositives().size();
                int tp = runEvalResults.getTruePositives().size();
                resultCalculator.addEvaluationResults(tp, fp, fn);
            }
        }
        return resultCalculator;
    }

    private ExplicitEvaluationResults<String> evaluateRun(Project project, ModelInstance removedElement, DataStructure data) {
        var modelId = data.getModelIds().get(0);

        ImmutableList<MissingModelInstanceInconsistency> inconsistencies = getInconsistencies(data, modelId);
        if (removedElement == null) {
            // base case
            // TODO
            return null;
        }

        var goldStandard = project.getGoldStandard(getPcmModel(project));
        var expectedLines = goldStandard.getSentencesWithElement(removedElement).distinct().collect(i -> i.toString()).castToCollection();
        var actualSentences = inconsistencies.collect(MissingModelInstanceInconsistency::sentence).distinct().collect(i -> i.toString()).castToCollection();

        return TestUtil.compare(actualSentences, expectedLines);
    }

    private PcmXMLModelConnector getPcmModel(Project project) {
        try {
            return new PcmXMLModelConnector(project.getModelFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ImmutableList<MissingModelInstanceInconsistency> getInconsistencies(DataStructure data, String modelId) {
        ImmutableList<Inconsistency> inconsistencies = data.getInconsistencyState(modelId).getInconsistencies();
        return inconsistencies.select(i -> MissingModelInstanceInconsistency.class.isAssignableFrom(i.getClass()))
                .collect(MissingModelInstanceInconsistency.class::cast);
    }

    private void logResults(Project project, EvaluationResults results, EvaluationResults expectedResults) {
        if (logger.isInfoEnabled()) {
            String infoString = String.format(Locale.ENGLISH,
                    "\n%s:\n\tPrecision:\t%.3f (min. expected: %.3f)%n\tRecall:\t\t%.3f (min. expected: %.3f)%n\tF1:\t\t%.3f (min. expected: %.3f)",
                    project.name(), results.getPrecision(), expectedResults.getPrecision(), results.getRecall(), expectedResults.getRecall(), results.getF1(),
                    expectedResults.getF1());
            logger.info(infoString);
        }
    }

    private void checkResults(EvaluationResults results, EvaluationResults expectedResults) {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(results.getPrecision() >= expectedResults.getPrecision(),
                        "Precision " + results.getPrecision() + " is below the expected minimum value " + expectedResults.getPrecision()), //
                () -> Assertions.assertTrue(results.getRecall() >= expectedResults.getRecall(),
                        "Recall " + results.getRecall() + " is below the expected minimum value " + expectedResults.getRecall()), //
                () -> Assertions.assertTrue(results.getF1() >= expectedResults.getF1(),
                        "F1 " + results.getF1() + " is below the expected minimum value " + expectedResults.getF1()));
    }

}
