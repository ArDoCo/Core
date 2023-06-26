package edu.kit.kastel.mcse.ardoco.core.api;

import java.io.File;

import edu.kit.kastel.mcse.ardoco.core.data.PipelineStepData;

/**
 * This {@link PipelineStepData} gives access to the diagram goldstandard associated with the project to analyze.
 */
public class InputDiagramDataMock implements PipelineStepData {
    public static final String ID = "InputDiagramDataMock";

    private final String diagramGoldStandard;

    public InputDiagramDataMock(File diagramGoldStandard) {
        this.diagramGoldStandard = diagramGoldStandard.getPath();
    }

    public InputDiagramDataMock(String diagramGoldStandard) {
        this.diagramGoldStandard = diagramGoldStandard;
    }

    public File getFile() {
        return new File(diagramGoldStandard);
    }

    public String getPath() {
        return diagramGoldStandard;
    }
}
