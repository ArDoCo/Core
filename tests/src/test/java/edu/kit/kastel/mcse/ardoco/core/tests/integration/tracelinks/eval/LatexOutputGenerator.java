package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class LatexOutputGenerator {

    record ThresholdFile(Path filePath, String group, int threshold) { }

    private final Path sourceDir;
    private final Path targetDir;

    public LatexOutputGenerator(Path sourceDir, Path targetDir) {
        this.sourceDir = sourceDir;
        this.targetDir = targetDir;
    }

    private ThresholdFile parseThresholdFile(Path filePath) {
        var fileName = filePath.getFileName().toString().replace(".json", "");
        var data = fileName.split("_");
        var thresholdStr = data[data.length - 1].replace("t", "");
        var threshold = Integer.parseInt(thresholdStr);
        var group = StringUtils.join(data, "_" , 0, data.length - 1);

        return new ThresholdFile(filePath, group, threshold);
    }

    public void run() throws IOException {
        Files.createDirectories(targetDir);

        var groupDirs = Files.list(sourceDir).filter(Files::isDirectory).toList();

        for (Path sourceGroupDir : groupDirs) {
            String group = sourceGroupDir.getFileName().toString();
            Files.createDirectories(targetDir.resolve(group));

            List<ThresholdFile> files = Files.list(sourceGroupDir).filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".json"))
                    .map(this::parseThresholdFile)
                    .sorted(Comparator.comparing(f -> f.threshold))
                    .toList();

            createMetricFile(group, files, "f1", EvalResult::getF1Score);
            createMetricFile(group, files, "precision", EvalResult::getPrecision);
            createMetricFile(group, files, "recall", EvalResult::getRecall);
            createMetricFile(group, files, "accuracy", EvalResult::getAccuracy);
        }
    }

//    private void appendToMetricFile(Path filePath, String metricId, double threshold, double metricValue) throws IOException {
//        //var filePath = directory.resolve(filePrefix + "_" + metricId + ".csv");
//        var fileContent = ((int) threshold * 100) + " " + ((int) (metricValue * 100)) + "\n";
//
//        if (Files.notExists(filePath)) {
//            Files.writeString(filePath, "threshold " + metricId + "\n", CREATE);
//        }
//
//        Files.writeString(filePath, fileContent, CREATE, APPEND);
//    }

    private void createMetricFile(String group, List<ThresholdFile> files, String metricId, Function<EvalResult, Double> metric) throws IOException {
        var fileName = String.format("%s_%s.csv", group, metricId);
        var filePath = targetDir.resolve(group).resolve(fileName);
        var fileContent = new StringBuilder("threshold ").append(metricId).append('\n');

        for (ThresholdFile file : files) {
            EvalResult result = EvalResult.fromJsonString(Files.readString(file.filePath));
            double metricValue = metric.apply(result);
            int roundedValue = (int) (metricValue * 100);

            fileContent.append(file.threshold).append(' ').append(roundedValue).append('\n');
        }

        Files.writeString(filePath, fileContent, CREATE, TRUNCATE_EXISTING);
    }

}
