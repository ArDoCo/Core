/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tlrhelper.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.ArDoCoResult;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;

public class TLModelFile {

    private TLModelFile() {
        // no instantiation
        throw new IllegalAccessError("No instantiation allowed");
    }

    public static void save(Path targetFile, Map<Project, ArDoCoResult> dataMap) throws IOException {
        var projects = dataMap.keySet().stream().sorted().toList();
        var builder = new StringBuilder();

        for (Project project : projects) {
            var projectData = dataMap.get(project);

            builder.append("# ").append(project.name()).append("\n\n");

            for (var modelId : projectData.getModelIds()) {
                var models = projectData.getModelState(modelId).getInstances();
                builder.append("## ModelId: ").append(modelId).append("\n");
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
                            .append(")\n");
                }
            }

            builder.append("\n\n");
        }

        Files.writeString(targetFile, builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

}
