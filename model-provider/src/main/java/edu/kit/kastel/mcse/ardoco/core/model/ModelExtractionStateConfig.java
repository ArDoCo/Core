package edu.kit.kastel.mcse.ardoco.core.model;

import edu.kit.kastel.mcse.ardoco.core.common.util.ResourceAccessor;

/**
 * The Class ModelExtractionStateConfig defines the configuration to be used for the model extraction.
 */
public final class ModelExtractionStateConfig {

    private ModelExtractionStateConfig() {
        throw new IllegalAccessError();
    }

    private static final ResourceAccessor CONFIG = loadParameters("/configs/modelExtractionState.properties");

    /**
     * The minimal amount of parts of the type that the type is splitted and can be identified by parts.
     */
    public static final int EXTRACTION_STATE_MIN_TYPE_PARTS = CONFIG.getPropertyAsInt("ExtractionState_MinTypeParts");

    private static ResourceAccessor loadParameters(String filePath) {
        return new ResourceAccessor(filePath, true);
    }
}
