/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.data;

import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.pipeline.Pipeline;

/**
 * Contains metadata about the pipeline which produced this data.
 */
public class MetaData implements PipelineStepData {
    public static final String ID = "PipelineMetaData";
    private Pipeline pipeline;
    private final WordSimUtils wordSimUtils;
    private final SimilarityUtils similarityUtils;

    /**
     * Constructs a new PipelineMetaData with the given meta data
     *
     * @param pipeline        the runner which produced the {@link edu.kit.kastel.mcse.ardoco.core.data.DataRepository DataRepository} this data is associated
     *                        with
     * @param wordSimUtils    the configured word similarity utility instance that should be used
     * @param similarityUtils the configured similarity util instance that should be used
     */
    public MetaData(Pipeline pipeline, WordSimUtils wordSimUtils, SimilarityUtils similarityUtils) {
        this.pipeline = pipeline;
        this.wordSimUtils = wordSimUtils;
        this.similarityUtils = similarityUtils;
    }

    /**
     * Constructs a new PipelineMetaData with the given meta data
     *
     * @param pipeline the pipeline which produced the {@link edu.kit.kastel.mcse.ardoco.core.data.DataRepository DataRepository} this data is associated with
     */
    public MetaData(Pipeline pipeline) {
        this.pipeline = pipeline;
        this.wordSimUtils = new WordSimUtils();
        this.similarityUtils = new SimilarityUtils(wordSimUtils);
    }

    public MetaData() {
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
