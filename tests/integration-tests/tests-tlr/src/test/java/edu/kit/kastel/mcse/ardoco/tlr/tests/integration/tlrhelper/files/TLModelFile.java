/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.tlr.tests.integration.tlrhelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;

/**
 * This helper-class offers functionality to write out information about the models as seen by ArDoCo after evaluation of TLR.
 */
public class TLModelFile {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private TLModelFile() {
        throw new IllegalAccessError("This constructor should not be called!");
    }

    /**
     * Writes out information about models to the target file.
     *
     * @param targetFile the file to write to
     * @param dataMap    the data map to extract model information for each project
     * @throws IOException if writing to file system fails
     */
    public static void save(Path targetFile, Map<GoldStandardProject, ArDoCoResult> dataMap) throws IOException {
        var projects = dataMap.keySet().stream().sorted().toList();
        var builder = new StringBuilder();

        for (GoldStandardProject project : projects) {
            var projectData = dataMap.get(project);

            builder.append("# ").append(project.getProjectName());
            builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);

            for (var modelId : projectData.getModelIds()) {
                var models = projectData.getModelState(modelId).getInstances();
                builder.append("## ModelId: ").append(modelId);
                builder.append(LINE_SEPARATOR);
                for (ModelInstance model : models) {
                    builder.append("- [")
                            .append(model.getUid())
                            .append("]: \"")
                            .append(model.getFullName())
                            .append("\" (")
                            .append(model.getFullType())
                            .append(") (")
                            .append(String.join(", ", model.getNameParts()))
                            .append(") (")
                            .append(String.join(", ", model.getTypeParts()))
                            .append(")")
                            .append(LINE_SEPARATOR);
                }
            }

            builder.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
