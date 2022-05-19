/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang.StringUtils;

/**
 * Creates .csv files that present various evaluation metrics at various thresholds. Each csv file has two columns. The
 * first column represents the threshold and the second column represents the value of some metric.
 */
public class EvalCSVGenerator {

    record ThresholdFile(Path filePath, String group, int threshold) {
    }

    private final Path sourceDir;
    private final Path targetDir;
    private final boolean overwriteExisting;

    public EvalCSVGenerator(Path sourceDir, Path targetDir, boolean overwriteExisting) {
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
        this.overwriteExisting = overwriteExisting;
    }

    static ThresholdFile parseThresholdFile(Path filePath) {
        var fileName = filePath.getFileName().toString().replace(".json", "");
        var data = fileName.split("_");
        var thresholdStr = data[data.length - 1].replace("t", "");
        var threshold = Integer.parseInt(thresholdStr);
        var group = StringUtils.join(data, "_", 0, data.length - 1);

        return new ThresholdFile(filePath, group, threshold);
    }

    public void run() throws IOException {
        Files.createDirectories(targetDir);

        var groupDirs = Files.list(sourceDir).filter(Files::isDirectory).toList();

        for (Path sourceGroupDir : groupDirs) {
            String group = sourceGroupDir.getFileName().toString();

            List<ThresholdFile> files = Files.list(sourceGroupDir)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .map(EvalCSVGenerator::parseThresholdFile)
                    .sorted(Comparator.comparing(f -> f.threshold))
                    .toList();

            createFractionalMetricFile(group, files, "f1", EvalResult::getF1Score);
            createFractionalMetricFile(group, files, "precision", EvalResult::getPrecision);
            createFractionalMetricFile(group, files, "recall", EvalResult::getRecall);
            createFractionalMetricFile(group, files, "accuracy", EvalResult::getAccuracy);
            createIntMetricFile(group, files, "tp", EvalResult::getTruePositiveCount);
            createIntMetricFile(group, files, "fp", EvalResult::getFalsePositiveCount);
            createIntMetricFile(group, files, "tn", EvalResult::getTrueNegativeCount);
            createIntMetricFile(group, files, "fn", EvalResult::getFalseNegativeCount);
        }
    }

    private void createIntMetricFile(String group, List<ThresholdFile> files, String metricId, Function<EvalResult, Integer> metric) throws IOException {
        var fileName = String.format("%s_%s.csv", group, metricId);
        var filePath = targetDir.resolve(fileName);
        var fileContent = new StringBuilder("threshold ").append(metricId).append('\n');

        if (!overwriteExisting && Files.exists(filePath)) {
            return;
        }

        for (ThresholdFile file : files) {
            EvalResult result = EvalResult.fromJsonString(Files.readString(file.filePath));
            int metricValue = metric.apply(result);

            fileContent.append(file.threshold).append(' ').append(metricValue).append('\n');
        }

        Files.writeString(filePath, fileContent, CREATE, TRUNCATE_EXISTING);
    }

    private void createFractionalMetricFile(String group, List<ThresholdFile> files, String metricId, Function<EvalResult, Double> metric) throws IOException {
        createIntMetricFile(group, files, metricId, (evalResult) -> (int) (metric.apply(evalResult) * 100) // turn
                                                                                                           // fraction
                                                                                                           // into
                                                                                                           // integer
        );
    }

}
