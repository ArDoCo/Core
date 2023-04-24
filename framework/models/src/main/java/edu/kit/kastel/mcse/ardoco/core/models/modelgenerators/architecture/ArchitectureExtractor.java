package edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.architecture;

import edu.kit.kastel.mcse.ardoco.core.models.amtl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.Extractor;

public abstract class ArchitectureExtractor extends Extractor {

    @Override
    public abstract ArchitectureModel extractModel(String path);
}
