/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.NewSimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.ComparisonStats;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;
import edu.kit.kastel.mcse.ardoco.core.tests.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Evaluates multiple {@link EvalPlan} instances one after another. Saves evaluation results to a specified directory.
 */
public class Evaluator {

    private final List<EvalPlan> plans;
    private final Path resultDir;

    /**
     * Constructs a new {@link Evaluator} instance.
     * 
     * @param plans     the plans to evaluate
     * @param resultDir in which directory to store the results
     */
    public Evaluator(List<EvalPlan> plans, Path resultDir) {
        this.plans = plans;
        this.resultDir = resultDir;

        if (plans.isEmpty()) {
            throw new IllegalArgumentException("no plans were provided to the evaluator");
        }
    }

    /**
     * Executes the evaluation process.
     * 
     * @throws IOException if the evaluation encounters an exception
     */
    public void execute() throws IOException {
        Files.createDirectories(this.resultDir);

        long totalStart = System.currentTimeMillis();

        for (EvalPlan plan : plans) {
            System.out.printf("%nEvaluating: %s%n%n", plan.getId());

            long evalStart = System.currentTimeMillis();

            var results = evaluatePlan(plan);

            long evalDuration = System.currentTimeMillis() - evalStart;

            System.out.printf("%nFinished %s ✔ (took %s seconds)%n", plan.getId(), evalDuration / 1000);

            saveResults(plan, results);
        }

        long totalDuration = System.currentTimeMillis() - totalStart;

        System.out.printf("%nEntire multi plan evaluation took %s seconds%n", totalDuration / 1000);
    }

    private EvaluationResults evaluatePlan(EvalPlan plan) throws IOException {
        var results = new EvaluationResults(0.0, 0.0, 0.0);

        ComparisonStats.ENABLED = false;
        NewSimilarityUtils.setMeasures(plan.getMeasures());

        for (Project project : Project.values()) {
            File modelFile = project.getTextOntologyFile();

            var data = Pipeline.run("test_" + project.name(), null, modelFile, null);

            var projectResult = new TLProjectEvalResult(project, data);

            results.f1 += projectResult.getF1();
            results.precision += projectResult.getPrecision();
            results.recall += projectResult.getRecall();

            System.out.printf("%n%s on %s: %s Precision, %s Recall, %s F1%n%n", plan.getId(), project.name().toUpperCase(), projectResult.getPrecision(),
                    projectResult.getRecall(), projectResult.getF1());
        }

        results.f1 /= Project.values().length;
        results.precision /= Project.values().length;
        results.recall /= Project.values().length;

        return results;
    }

    private void saveResults(EvalPlan plan, EvaluationResults results) throws IOException {
        var filePrefix = plan.getGroup() + "_b" + plan.getBase();

        var f1File = resultDir.resolve(filePrefix + "_f1.dat");
        var precisionFile = resultDir.resolve(filePrefix + "_precision.dat");
        var recallFile = resultDir.resolve(filePrefix + "_recall.dat");

        if (Files.notExists(f1File)) {
            Files.writeString(f1File, "x y\n", CREATE);
        }

        if (Files.notExists(precisionFile)) {
            Files.writeString(precisionFile, "x y\n", CREATE);
        }

        if (Files.notExists(recallFile)) {
            Files.writeString(recallFile, "x y\n", CREATE);
        }

        Files.writeString(f1File, plan.getThreshold() + " " + ((int) (results.getF1() * 100)) + "\n", CREATE, APPEND);
        Files.writeString(precisionFile, plan.getThreshold() + " " + ((int) (results.getPrecision() * 100)) + "\n", CREATE, APPEND);
        Files.writeString(recallFile, plan.getThreshold() + " " + ((int) (results.getRecall() * 100)) + "\n", CREATE, APPEND);
    }

}
