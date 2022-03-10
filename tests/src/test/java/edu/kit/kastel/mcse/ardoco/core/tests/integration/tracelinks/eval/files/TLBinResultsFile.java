package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files;

import edu.kit.kastel.mcse.ardoco.core.tests.EvaluationResults;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.TLProjectEvalResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class TLBinResultsFile {

    public static void save(Path targetFile, Collection<TLProjectEvalResult> results) throws IOException {
        var builder = new StringBuilder();

        for (TLProjectEvalResult result : results) {
            builder.append(result.getProject().name()).append('\n');
            builder.append(result.getPrecision()).append('\n');
            builder.append(result.getRecall()).append('\n');
            builder.append(result.getF1()).append('\n');
        }

        Files.writeString(targetFile, builder.toString(), CREATE, TRUNCATE_EXISTING);
    }

    public static Map<Project, EvaluationResults> read(Path targetFile) throws IOException {
        Map<Project, EvaluationResults> map = new HashMap<>();

        var iterator = Files.readAllLines(targetFile).iterator();

        while (iterator.hasNext()) {
            Project project = Project.valueOf(iterator.next());
            double precision = Double.parseDouble(iterator.next());
            double recall = Double.parseDouble(iterator.next());
            double f1 = Double.parseDouble(iterator.next());

            map.put(project, new EvaluationResults(precision, recall, f1));
        }

        return map;
    }

}
