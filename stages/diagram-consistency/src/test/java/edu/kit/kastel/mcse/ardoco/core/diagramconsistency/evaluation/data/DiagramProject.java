/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation.data;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;

/**
 * All available evaluation diagrams.
 */
public enum DiagramProject {
    /**
     * The BigBlueButton architecture diagram.
     */
    BIG_BLUE_BUTTON("bigbluebutton", CodeProject.BIGBLUEBUTTON, ArchitectureModelType.UML),
    /**
     * The team-mates architecture diagram.
     */
    TEAMMATES_ARCHITECTURE("teammates_architecture", CodeProject.TEAMMATES, ArchitectureModelType.UML),
    /**
     * The team-mates package diagram.
     */
    TEAMMATES_PACKAGES("teammates_packages", CodeProject.TEAMMATES, CodeModelType.CODE_MODEL),
    /**
     * The team-mates UI package/class diagram.
     */
    TEAMMATES_UI("teammates_ui", CodeProject.TEAMMATES, CodeModelType.CODE_MODEL),
    /**
     * The tea-store architecture diagram.
     */
    TEA_STORE("teastore", CodeProject.TEASTORE, ArchitectureModelType.UML),
    /**
     * The media-store architecture diagram.
     */
    MEDIA_STORE("mediastore", CodeProject.MEDIASTORE, ArchitectureModelType.UML);

    private static final String FILE_EXTENSION = ".json";
    private static final String DIAGRAM_FILE_NAME = "diagram";
    private static final String IDENTIFICATION_STAGE_FILE_NAME = "gs_stage1";
    private static final String LINKING_STAGE_FILE_NAME = "gs_stage2";
    private static final String VALIDATION_STAGE_FILE_NAME = "gs_stage3";
    private final String path;
    private final CodeProject sourceProject;
    private final ModelType modelType;

    DiagramProject(String path, CodeProject sourceProject, ModelType modelType) {
        this.path = path;
        this.sourceProject = sourceProject;
        this.modelType = modelType;
    }

    /**
     * Gets the source project, which is the project in which the diagram is part of the documentation.
     *
     * @return The source project.
     */
    public CodeProject getSourceProject() {
        return this.sourceProject;
    }

    /**
     * Gets the model type of the diagram.
     *
     * @return The model type.
     */
    public ModelType getModelType() {
        return this.modelType;
    }

    /**
     * Get the diagram file.
     *
     * @return The diagram file.
     */
    public File getDiagramFile() {
        URL resource = this.getDeclaringClass().getResource(this.getPath(DIAGRAM_FILE_NAME));

        if (resource == null) {
            throw new IllegalArgumentException("Could not find diagram file for " + this.name());
        }

        return new File(resource.getFile());
    }

    /**
     * Gets the diagram file content.
     *
     * @return The text in the file.
     * @throws IOException
     *                     If the file could not be read.
     */
    public String getDiagram() throws IOException {
        return this.readFromPath(this.getPath(DIAGRAM_FILE_NAME));
    }

    /**
     * Gets the identification stage evaluation file content.
     *
     * @return The text in the file.
     * @throws IOException
     *                     If the file could not be read.
     */
    public String getIdentificationStage() throws IOException {
        return this.readFromPath(this.getPath(IDENTIFICATION_STAGE_FILE_NAME));
    }

    /**
     * Gets the linking stage evaluation file content.
     *
     * @return The text in the file.
     * @throws IOException
     *                     If the file could not be read.
     */
    public String getLinkingStage() throws IOException {
        return this.readFromPath(this.getPath(LINKING_STAGE_FILE_NAME));
    }

    /**
     * Gets the validation stage evaluation file content.
     *
     * @return The text in the file.
     * @throws IOException
     *                     If the file could not be read.
     */
    public String getValidationStage() throws IOException {
        return this.readFromPath(this.getPath(VALIDATION_STAGE_FILE_NAME));
    }

    private String getPath(String name) {
        return String.format("/gs/%s/%s%s", this.path, name, FILE_EXTENSION);
    }

    private String readFromPath(String path) throws IOException {
        try (InputStream stream = this.getDeclaringClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IOException("Could not create stream for path " + path);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
