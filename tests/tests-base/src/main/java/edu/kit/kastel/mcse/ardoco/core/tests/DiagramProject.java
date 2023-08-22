/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import edu.kit.kastel.mcse.ardoco.core.tests.eval.CodeProject;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.Project;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.ProjectHelper;

public enum DiagramProject {
    TEASTORE(CodeProject.TEASTORE, "/diagrams/teastore/teastore-paper.png");

    private final CodeProject codeProject;
    private final String architectureDiagram;

    DiagramProject(CodeProject codeProject, String architectureDiagram) {
        this.codeProject = codeProject;
        this.architectureDiagram = architectureDiagram;

    }

    public static DiagramProject byCodeProject(CodeProject codeProject) {
        for (DiagramProject diagramProject : values()) {
            if (diagramProject.codeProject == codeProject) {
                return diagramProject;
            }
        }
        throw new IllegalArgumentException("No diagram project for code project " + codeProject);
    }

    public File getDiagramDirectory() {
        var architectureFile = ProjectHelper.loadFileFromResources(architectureDiagram);
        try {
            var name = architectureDiagram.substring(architectureDiagram.lastIndexOf('/') + 1);
            File architectureDirectory = Files.createTempDirectory("ArDoCo-architecture").toFile();
            File architectureFileDestination = new File(architectureDirectory, name);
            Files.copy(architectureFile.toPath(), architectureFileDestination.toPath());
            return architectureDirectory;
        } catch (IOException e) {
            throw new IllegalStateException("Could not create temporary directory for architecture diagram", e);
        }
    }

    public CodeProject getCodeProject() {
        return codeProject;
    }

    public Project getProject() {
        return codeProject.getProject();
    }
}
