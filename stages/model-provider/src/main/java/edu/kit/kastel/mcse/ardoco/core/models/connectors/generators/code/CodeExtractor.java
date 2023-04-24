package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code;

import edu.kit.kastel.mcse.ardoco.core.api.models.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.Extractor;

public abstract class CodeExtractor extends Extractor {

    @Override
    public abstract CodeModel extractModel(String path);
}
