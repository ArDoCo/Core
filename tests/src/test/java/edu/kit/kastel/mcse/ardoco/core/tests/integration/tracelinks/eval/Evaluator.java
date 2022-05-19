/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.ComparisonStats;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;

/**
 * Evaluates multiple {@link EvalPlan} instances one after another. Saves evaluation results to a specified directory.
 */
public class Evaluator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);

    private final List<EvalPlan> plans;
    private final Path resultDir;
    private final boolean overwriteExistingResults;

    /**
     * Constructs a new {@link Evaluator} instance.
     * 
     * @param plans     the plans to evaluate
     * @param resultDir in which directory to store the results
     */
    public Evaluator(List<EvalPlan> plans, Path resultDir, boolean overwriteExistingResults) {
        this.plans = plans;
        this.resultDir = resultDir;
        this.overwriteExistingResults = overwriteExistingResults;

        if (plans.isEmpty()) {
            throw new IllegalArgumentException("no plans were provided to the evaluator");
        }
    }

    /**
     * Executes the evaluation process.
     * 
     * @throws IOException if the evaluation encounters an exception
     */
    public void execute() throws IOException, ReflectiveOperationException {
        Files.createDirectories(this.resultDir);

        long totalStart = System.currentTimeMillis();

        for (EvalPlan plan : plans) {
            // Prepare directory & result file
            Path groupDir = this.resultDir.resolve(plan.getGroup());
            Files.createDirectories(groupDir);

            Path resultFile = groupDir.resolve(plan.getId() + ".json");
            if (!overwriteExistingResults && Files.exists(resultFile)) {
                LOGGER.warn("Skipping evaluation for {} because the result file already exists!", plan.getId());
                continue;
            }

            // Perform the evaluation
            LOGGER.info("Evaluating {}", plan.getId());

            long evalStart = System.currentTimeMillis();

            EvalPlanResult result = evaluatePlan(plan);

            long evalDuration = System.currentTimeMillis() - evalStart;

            LOGGER.info("Finished {} ✔ (took {} seconds)", plan.getId(), evalDuration / 1000);

            // Save the evaluation results to file
            Files.writeString(resultFile, result.result().toJsonString(), CREATE, TRUNCATE_EXISTING);
        }

        long totalDuration = System.currentTimeMillis() - totalStart;

        LOGGER.info("Entire multi plan evaluation took {} seconds", totalDuration / 1000);
    }

    private EvalPlanResult evaluatePlan(EvalPlan plan) throws IOException, ReflectiveOperationException {
        ComparisonStats.ENABLED = false;
        WordSimUtils.setMeasures(plan.getMeasures());

        var projectResults = new ArrayList<EvalProjectResult>();

        for (Project project : Project.values()) {
            EvalProjectResult result = evaluateProject(project);

            projectResults.add(result);

            LOGGER.info("{} on {}: {} Precision, {} Recall, {} F1, {} Accuracy", plan.getId(), project.name().toUpperCase(), result.getPrecision(),
                    result.getRecall(), result.getF1Score(), result.getAccuracy());
        }

        var evalResult = new EvalResult(projectResults);

        return new EvalPlanResult(plan, evalResult);
    }

    private EvalProjectResult evaluateProject(Project project) throws IOException, ReflectiveOperationException {
        File modelFile = project.getModelFile();
        File textFile = project.getPreprocessedTextFile();
        boolean usePreprocessedText = true;

        if (!textFile.exists()) {
            textFile = project.getTextFile();
            usePreprocessedText = false;
        }

        var data = Pipeline.run("test_" + project.name(), textFile, usePreprocessedText, modelFile, null);

        return new EvalProjectResult(project, data);
    }

}
