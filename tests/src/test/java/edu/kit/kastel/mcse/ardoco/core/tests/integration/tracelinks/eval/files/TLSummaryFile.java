/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.files;

import edu.kit.kastel.mcse.ardoco.core.api.data.DataStructure;
import edu.kit.kastel.mcse.ardoco.core.tests.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.EvalProjectResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.EvalResult;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.EvalUtils;
import edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.TestLink;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class TLSummaryFile {

	private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("##0.00%");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	public static void save(Path targetFile, EvalResult result, Map<Project, DataStructure> dataMap)
		throws IOException {

		var file = new StringBuilder();

		file.append("# Summary\n\n");
		file.append("- Time of evaluation: `").append(DATE_FORMATTER.format(LocalDateTime.now())).append("`\n");

		appendMetrics(file, result.getPrecision(), result.getRecall(), result.getF1Score(),
			result.getAccuracy(), result.getFalsePositiveCount(), result.getFalseNegativeCount(),
			result.getTruePositiveCount(), result.getTrueNegativeCount()
		);

		for (EvalProjectResult projectResult : result.getProjectResults()) {
			file.append("\n## ").append(projectResult.getProject().name()).append("\n\n");

			appendMetrics(file, projectResult.getPrecision(), projectResult.getRecall(), projectResult.getF1Score(),
				projectResult.getAccuracy(),
				projectResult.getFalsePositives().size(), projectResult.getFalseNegatives().size(),
				projectResult.getTruePositives().size(), projectResult.getTrueNegativeCount()
			);

			if (!projectResult.getFalsePositives().isEmpty()) {
				file.append("\nFalse Positives:\n");
				appendLinks(file, projectResult.getFalsePositives(), dataMap.get(projectResult.getProject()));
			}

			if (!projectResult.getFalseNegatives().isEmpty()) {
				file.append("\nFalse Negatives:\n");
				appendLinks(file, projectResult.getFalseNegatives(), dataMap.get(projectResult.getProject()));
			}
		}

		Files.writeString(targetFile, file.toString(), UTF_8, CREATE, TRUNCATE_EXISTING);
	}

	private static void appendLinks(StringBuilder file, List<TestLink> links, DataStructure data) {
		for (TestLink link : links) {
			file.append("- ");
			String str = EvalUtils.formatLink(link, data);
			file.append(str);
			file.append('\n');
		}
	}

	private static void appendMetrics(StringBuilder file, double precision, double recall, double f1, double accuracy,
	                                  int falsePositives, int falseNegatives, int truePositives, int trueNegatives) {
		file.append(format("- %s Precision, ", NUMBER_FORMAT.format(precision)));
		file.append(format("%s Recall, ", NUMBER_FORMAT.format(recall)));
		file.append(format("%s F1, ", NUMBER_FORMAT.format(f1)));
		file.append(format("%s Accuracy\n", NUMBER_FORMAT.format(accuracy)));

		file.append(format("- %s False Positives, ", falsePositives));
		file.append(format("%s False Negatives, ", falseNegatives));
		file.append(format("%s True Positives, ", truePositives));
		file.append(format("%s True Negatives\n", trueNegatives));
	}

	private TLSummaryFile() { }

}
