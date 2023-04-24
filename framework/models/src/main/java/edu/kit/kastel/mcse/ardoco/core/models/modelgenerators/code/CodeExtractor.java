package edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.code;

import edu.kit.kastel.mcse.ardoco.core.models.cmtl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.models.modelgenerators.Extractor;

public abstract class CodeExtractor extends Extractor {

    @Override
    public abstract CodeModel extractModel(String path);
}
