/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.data;

import edu.kit.kastel.mcse.ardoco.core.common.similarity.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.common.similarity.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

/**
 * Contains global configuration about the pipeline which produced this data.
 */
public class GlobalConfiguration implements PipelineStepData {
    public static final String ID = "PipelineMetaData";
    private Pipeline pipeline;
    private final WordSimUtils wordSimUtils;
    private final SimilarityUtils similarityUtils;

    /**
     * Constructs a new PipelineMetaData with the given global configuration data
     *
     * @param pipeline        the runner which produced the {@link DataRepository} this data is associated with
     * @param wordSimUtils    the configured word similarity utility instance that should be used
     * @param similarityUtils the configured similarity util instance that should be used
     */
    public GlobalConfiguration(Pipeline pipeline, WordSimUtils wordSimUtils, SimilarityUtils similarityUtils) {
        this.pipeline = pipeline;
        this.wordSimUtils = wordSimUtils;
        this.similarityUtils = similarityUtils;
    }

    /**
     * Constructs a new PipelineMetaData with the given global configuration
     *
     * @param pipeline the pipeline which produced the {@link DataRepository} this data is associated with
     */
    public GlobalConfiguration(Pipeline pipeline) {
        this.pipeline = pipeline;
        this.wordSimUtils = new WordSimUtils();
        this.similarityUtils = new SimilarityUtils(wordSimUtils);
    }

    public GlobalConfiguration() {
        this.wordSimUtils = new WordSimUtils();
        this.similarityUtils = new SimilarityUtils(wordSimUtils);
    }

    /**
     * {@return the runner which produced the DataRepository this data is associated with}
     */
    public Pipeline getPipeline() {
        return this.pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * {@return the configured word similarity utility instance}
     */
    public WordSimUtils getWordSimUtils() {
        return this.wordSimUtils;
    }

    /**
     * {@return the configured similarity utility instance}
     */
    public SimilarityUtils getSimilarityUtils() {
        return this.similarityUtils;
    }
}
