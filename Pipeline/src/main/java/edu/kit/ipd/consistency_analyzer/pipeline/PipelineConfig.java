package edu.kit.ipd.consistency_analyzer.pipeline;

import edu.kit.ipd.consistency_analyzer.common.SystemParameters;

public class PipelineConfig {

    private PipelineConfig() {
        throw new IllegalAccessError();
    }

    private static final SystemParameters CONFIG = loadParameters("/configs/Pipeline.properties");

    /**
     * The path to read the textual input (documentation) from.
     */
    public static final String DOCUMENTATION_PATH = CONFIG.getProperty("documentation_Path");

    /**
     * The path to a text textual input.
     */
    public static final String TEST_DOCUMENTATION_PATH = CONFIG.getProperty("testDocumentation_Path");
    /**
     * The path to write the read in input in.
     */
    public static final String FILE_FOR_INPUT_PATH = CONFIG.getProperty("fileForInput_Path");

    /**
     * The path to write the results in.
     */
    public static final String FILE_FOR_RESULTS_PATH = CONFIG.getProperty("fileForResults_Path");

    public static final String FILE_FOR_CSV_RESULTS_PATH = CONFIG.getProperty("fileForCSVResults_Path");

    private static SystemParameters loadParameters(String filePath) {
        return new SystemParameters(filePath, true);
    }
}
