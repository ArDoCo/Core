package edu.kit.kastel.mcse.ardoco.core.textextractor;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.ResourceAccessor;

/**
 * The Class TextExtractorConfig defines the configuration for the extractors of this stage.
 */
public class TextExtractorConfig extends Configuration {

    private static final String CONFIGS_TEXT_EXTRACTOR_PROPERTIES = "/configs/TextExtractor.properties";
    private static final String DELIMITER = " ";
    private static final String SIMILARITY_PERCENTAGE = "similarityPercentage";
    private static final String TEXT_AGENTS = "Text_Agents";

    /** The Constant DEFAULT_CONFIG. */
    public static final TextExtractorConfig DEFAULT_CONFIG = new TextExtractorConfig();

    /**
     * The list of text extraction agent types that should run.
     */
    public final List<String> textAgents;

    // TODO @Sophie details
    /** The similarity percentage in the state. */
    public final double similarityPercentage;

    /**
     * Instantiates a new text extractor config.
     */
    public TextExtractorConfig() {
        var config = new ResourceAccessor(CONFIGS_TEXT_EXTRACTOR_PROPERTIES, true);
        textAgents = config.getPropertyAsList(TEXT_AGENTS);
        similarityPercentage = config.getPropertyAsDouble(SIMILARITY_PERCENTAGE);
    }

    /**
     * Instantiates a new text extractor config.
     *
     * @param configs the configs
     */
    public TextExtractorConfig(Map<String, String> configs) {
        textAgents = getPropertyAsList(TEXT_AGENTS, configs);
        similarityPercentage = getPropertyAsDouble(SIMILARITY_PERCENTAGE, configs);
    }

    @Override
    protected Map<String, String> getAllProperties() {
        return Map.of(//
                TEXT_AGENTS, String.join(DELIMITER, textAgents), //
                SIMILARITY_PERCENTAGE, String.valueOf(similarityPercentage) //
        );
    }
}
