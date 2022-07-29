/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fasttext;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.RetrieveVectorException;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorBasedWordSimMeasure;

/**
 * A word similarity measure based on the fastText neural network. It grabs the vectors for each word and compares them
 * using cosine similarity. This measure additionally manages a cache to improve lookup speeds.
 */
public class FastTextMeasure extends VectorBasedWordSimMeasure {

    private static final Logger LOGGER = LoggerFactory.getLogger(FastTextMeasure.class);

    private final double similarityThreshold;

    /**
     * Constructs a new {@link FastTextMeasure} using the settings provided by
     * {@link edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig}.
     */
    public FastTextMeasure() {
        this(new DL4JFastTextDataSource(Path.of(CommonTextToolsConfig.FASTTEXT_MODEL_FILE_PATH)), CommonTextToolsConfig.FASTTEXT_SIMILARITY_THRESHOLD);
    }

    /**
     * Constructs a new {@link FastTextMeasure} instance.
     *
     * @param dataSource          the data source from which word vectors are loaded
     * @param similarityThreshold the threshold above which words are considered similar, between 0 and 1
     * @throws IllegalArgumentException if the given threshold is not between 0 and 1
     */
    public FastTextMeasure(DL4JFastTextDataSource dataSource, double similarityThreshold) throws IllegalArgumentException {
        super(dataSource);
        this.similarityThreshold = similarityThreshold;

        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("similarityThreshold outside of valid range: " + similarityThreshold);
        }
    }

    @Override
    public boolean areWordsSimilar(ComparisonContext ctx) {
        double similarity = Double.NaN;

        try {
            similarity = compareVectors(ctx.firstTerm(), ctx.secondTerm());
        } catch (RetrieveVectorException e) {
            LOGGER.error("failed to compare fastText vectors: " + ctx, e);
        }

        return similarity >= this.similarityThreshold;
    }

}
