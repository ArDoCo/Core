package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture;

import edu.kit.kastel.mcse.ardoco.core.api.models.architecture.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.Extractor;

public abstract class ArchitectureExtractor extends Extractor {

    @Override
    public abstract ArchitectureModel extractModel(String path);
}
