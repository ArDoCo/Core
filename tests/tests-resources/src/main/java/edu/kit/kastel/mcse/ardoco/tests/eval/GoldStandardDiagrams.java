package edu.kit.kastel.mcse.ardoco.tests.eval;

import java.io.File;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramGS;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;

public interface GoldStandardDiagrams extends GoldStandardProject {
    String getDiagramsResourceName();
    File getDiagramsGoldStandardFile();
    Set<DiagramGS> getDiagrams();
}
