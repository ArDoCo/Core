package edu.kit.kastel.mcse.ardoco.tests.eval;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramGS;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.GoldStandardProject;
import java.io.File;
import java.util.List;
import java.util.Set;

public interface GoldStandardDiagrams extends GoldStandardProject {
    String getDiagramsGoldStandardResourceName();

    File getDiagramsGoldStandardFile();

    Set<DiagramGS> getDiagramsGoldStandard();

    public Set<String> getDiagramResourceNames();

    public List<Pair<String, File>> getDiagramData();
}
