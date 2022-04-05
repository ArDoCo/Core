/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.ComparisonStats;
import edu.kit.kastel.mcse.ardoco.core.common.util.VectorUtils;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A word similarity measure based on the fastText neural network.
 * It grabs the vectors for each word and compares them using cosine similarity.
 * This measure additionally manages a cache to improve lookup speeds.
 */
public class FastTextMeasure implements WordSimMeasure {

    private static final double[] ZERO_VECTOR = new double[0];

    private final DL4JFastTextDataSource dataSource;
    private final double similarityThreshold;
    private final Map<String, double[]> cache = new HashMap<>();

    /**
     * Constructs a new {@link FastTextMeasure} using the settings provided by {@link edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig}.
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
        this.dataSource = Objects.requireNonNull(dataSource);
        this.similarityThreshold = similarityThreshold;

        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("similarityThreshold outside of valid range: " + similarityThreshold);
        }
    }

    @Override public boolean areWordsSimilar(ComparisonContext ctx) {
        double[] firstVec = this.cache.get(ctx.firstTerm());

        if (firstVec == null) {
            firstVec = this.dataSource.getWordVector(ctx.firstTerm()).orElse(ZERO_VECTOR);
            this.cache.put(ctx.firstTerm(), firstVec);
        }

        if (VectorUtils.isZero(firstVec)) {
            return false; // no vector representation for the first term
        }

        double[] secondVec = this.cache.get(ctx.secondTerm());

        if (secondVec == null) {
            secondVec = this.dataSource.getWordVector(ctx.secondTerm()).orElse(ZERO_VECTOR);
            this.cache.put(ctx.secondTerm(), secondVec);
        }

        if (VectorUtils.isZero(secondVec)) {
            return false; // no vector representation for the second term
        }

        double similarity = VectorUtils.cosineSimilarity(firstVec, secondVec);

        ComparisonStats.recordScore(similarity);

        return similarity >= this.similarityThreshold;
    }

}
