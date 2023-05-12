/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.Extractor;

public abstract class CodeExtractor extends Extractor {

    protected CodeExtractor(String path) {
        super(path);
    }

    @Override
    public abstract CodeModel extractModel();

    @Override
    public ModelType getModelType() {
        return CodeModelType.CODE_MODEL;
    }

}