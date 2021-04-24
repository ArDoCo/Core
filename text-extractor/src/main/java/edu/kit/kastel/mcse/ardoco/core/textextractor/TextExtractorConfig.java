package edu.kit.kastel.mcse.ardoco.core.textextractor;

import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.util.SystemParameters;

public class TextExtractorConfig extends Configuration {

    public static final TextExtractorConfig DEFAULT_CONFIG = new TextExtractorConfig();

    /**
     * The list of text extraction agent types that should run.
     */
    public final List<String> textAgents;

    public final double similarityPercentage;

    public TextExtractorConfig() {
        SystemParameters config = new SystemParameters("/configs/TextExtractor.properties", true);
        textAgents = config.getPropertyAsList("Text_Agents");
        similarityPercentage = config.getPropertyAsDouble("similarityPercentage");
    }

    public TextExtractorConfig(Map<String, String> configs) {
        textAgents = getPropertyAsList("Text_Agents", configs);
        similarityPercentage = getPropertyAsDouble("similarityPercentage", configs);
    }
}
