package edu.kit.kastel.mcse.ardoco.core.tests.integration.statehelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;

public class RecommendationStateFile {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private static final String VALUE_SEPARATOR = "|";
    private static final String LIST_SEPARATOR = ",";

    private RecommendationStateFile() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Writes out the differences of new and old results.
     *
     * @param targetFile   file to write into
     * @param arDoCoResult the ArDoCoResult
     * @throws IOException if writing fails
     */
    public static void save(Path targetFile, ArDoCoResult arDoCoResult, String modelId) throws IOException {

        var metaModel = arDoCoResult.getModelState(modelId).getMetamodel();
        var builder = new StringBuilder();

        var recommendationState = arDoCoResult.getRecommendationState(metaModel);

        builder.append("# ").append(arDoCoResult.getProjectName());
        builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

        builder.append(
                "Name" + VALUE_SEPARATOR + "Type" + VALUE_SEPARATOR + "Probaiblity" + VALUE_SEPARATOR + "Names" + VALUE_SEPARATOR + "Types" + VALUE_SEPARATOR + "Claimants" + LINE_SEPARATOR);
        builder.append(LINE_SEPARATOR);

        for (RecommendedInstance recommendation : recommendationState.getRecommendedInstances()) {
            builder.append(recommendation.getName());
            builder.append(VALUE_SEPARATOR);
            builder.append(recommendation.getType());
            builder.append(VALUE_SEPARATOR);
            builder.append(recommendation.getProbability());
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, recommendation.getNameMappings().collect(NounMapping::getReference)));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, recommendation.getTypeMappings().collect(NounMapping::getReference)));
            builder.append(VALUE_SEPARATOR);
            builder.append(String.join(LIST_SEPARATOR, recommendation.getClaimants().collect(c -> c.getClass().getSimpleName())));
            builder.append(LINE_SEPARATOR);

        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

    }

}
