package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.glove;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.ComparisonStats;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This word similarity measures utilizes GloVe trained word vector representations to calculate word similarity.
 * It retrieves vectors for each word and compares them using cosine similarity.
 * This measure additionally manages a cache to improve lookup speeds.
 */
public class GloveMeasure implements WordSimMeasure {

	// TODO: Extend from VectorBasedWordSimMeasure

    private static final Logger LOGGER = LoggerFactory.getLogger(GloveMeasure.class);
    private static final float[] ZERO_VECTOR = new float[0];

    private final GloveSqliteDataSource dataSource;
    private final double similarityThreshold;
    private final Map<String, float[]> cache = new HashMap<>();

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
     * @param dataSource          the data source from which word vectors are loaded
     * @param similarityThreshold the threshold above which words are considered similar, between 0 and 1
     * @throws IllegalArgumentException if the given threshold is not between 0 and 1
     */
    public GloveMeasure(GloveSqliteDataSource dataSource, double similarityThreshold) throws IllegalArgumentException {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.similarityThreshold = similarityThreshold;

        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("similarityThreshold outside of valid range: " + similarityThreshold);
        }
    }

    @Override public boolean areWordsSimilar(ComparisonContext ctx) {
        try {
            float[] firstVector = this.cache.get(ctx.firstTerm());

            if (firstVector == null) {
                firstVector = this.dataSource.getWordVector(ctx.firstTerm()).orElse(ZERO_VECTOR);
                this.cache.put(ctx.firstTerm(), firstVector);
            }

            if (VectorUtils.isZero(firstVector)) {
                return false;
            }

            float[] secondVector = this.cache.get(ctx.secondTerm());

            if (secondVector == null) {
                secondVector = this.dataSource.getWordVector(ctx.secondTerm()).orElse(ZERO_VECTOR);
                this.cache.put(ctx.secondTerm(), secondVector);
            }

            if (VectorUtils.isZero(secondVector)) {
                return false;
            }

            double similarity = VectorUtils.cosineSimilarity(firstVector, secondVector);

            ComparisonStats.recordScore(similarity);

            return similarity >= this.similarityThreshold;
        } catch (SQLException e) {
            LOGGER.error("Failed to query the glove database for a word vector", e);
            return false;
        }
    }

}
