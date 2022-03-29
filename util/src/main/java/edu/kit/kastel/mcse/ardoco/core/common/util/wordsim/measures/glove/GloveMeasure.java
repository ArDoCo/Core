package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.glove;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.stats.ComparisonStats;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This word similarity measures utilizes GloVe trained word vector representations to calculate word similarity.
 */
public class GloveMeasure implements WordSimMeasure {

    private static final Logger LOGGER = LoggerFactory.getLogger(GloveMeasure.class);

    private final GloveSqliteDataSource dataSource;
    private final double similarityThreshold;
    private final Map<String, float[]> cache = new HashMap<>();
    private final float[] zeroVector = new float[0];

    /**
     * Constructs a new {@link GloveMeasure} using the settings provided by {@link CommonTextToolsConfig}.
     *
     * @throws SQLException if establishing the connection to the data source fails
     */
    public GloveMeasure() throws SQLException {
        this(new GloveSqliteDataSource(Path.of(CommonTextToolsConfig.GLOVE_DB_FILE_PATH)), CommonTextToolsConfig.GLOVE_SIMILARITY_THRESHOLD);
    }

    /**
     * Constructs a new {@link GloveMeasure} instance.
     *
     * @param dataSource          the data source from which word similarities are loaded
     * @param similarityThreshold the threshold above which words are considered similar, between 0 and 1
     */
    public GloveMeasure(GloveSqliteDataSource dataSource, double similarityThreshold) {
        this.dataSource = dataSource;
        this.similarityThreshold = similarityThreshold;
    }

    @Override public boolean areWordsSimilar(ComparisonContext ctx) {
//        double minSimilarity = cosineCompare(ctx.firstTerm(), ctx.secondTerm()).orElse(Double.MIN_VALUE);

        double minSimilarity = Double.NaN;
        for (String firstTerm : ctx.firstTerms()) {
            for (String secondTerm : ctx.secondTerms()) {
                var similarity = cosineCompare(firstTerm, secondTerm).orElse(Double.NaN);

                if (Double.isNaN(similarity)) {
                    return false; // Can't recognize part of one of the terms
                }

                minSimilarity = Double.isNaN(minSimilarity) ? similarity : Math.min(minSimilarity, similarity);
            }
        }

        ComparisonStats.recordScore(minSimilarity);

        return minSimilarity >= this.similarityThreshold;
    }

    private Optional<Double> cosineCompare(String first, String second) {
        try {
            float[] firstVector = this.cache.get(first);
            if (firstVector == null) {
                firstVector = this.dataSource.getWordVector(first).orElse(zeroVector);
                this.cache.put(first, firstVector);
            }
            if (VectorUtils.isZero(firstVector)) {
                return Optional.empty();
            }

            float[] secondVector = this.cache.get(second);
            if (secondVector == null) {
                secondVector = this.dataSource.getWordVector(second).orElse(zeroVector);
                this.cache.put(second, secondVector);
            }
            if (VectorUtils.isZero(secondVector)) {
                return Optional.empty();
            }

            return Optional.of(VectorUtils.cosineSimilarity(firstVector, secondVector));
        } catch (SQLException e) {
            LOGGER.error("Failed to query the glove database for a word vector", e);
            return Optional.empty();
        }
    }

}
