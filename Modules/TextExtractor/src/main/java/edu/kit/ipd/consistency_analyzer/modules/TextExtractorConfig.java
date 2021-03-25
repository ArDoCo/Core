package edu.kit.ipd.consistency_analyzer.modules;

import java.util.List;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.Configuration;
import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class TextExtractorConfig extends Configuration {

    public static final TextExtractorConfig DEFAULT_CONFIG = new TextExtractorConfig();

    /**
     * The list of text extraction agent types that should run.
     */
    public final List<String> textAgents;

    public TextExtractorConfig() {
        SystemParameters config = new SystemParameters("/configs/TextExtractor.properties", true);
        textAgents = config.getPropertyAsList("Text_Agents");
    }

    public TextExtractorConfig(List<String> textAgents) {
        this.textAgents = textAgents;
    }

}
