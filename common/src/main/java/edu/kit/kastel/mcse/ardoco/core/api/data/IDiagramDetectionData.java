/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.IDiagramDetectionState;

import java.io.File;

public interface IDiagramDetectionData extends IData {
    void setDiagramDirectory(String directory);

    File getDiagramDirectory();

    void setDiagramDetectionState(IDiagramDetectionState state);

    IDiagramDetectionState getDiagramDetectionState();
}
