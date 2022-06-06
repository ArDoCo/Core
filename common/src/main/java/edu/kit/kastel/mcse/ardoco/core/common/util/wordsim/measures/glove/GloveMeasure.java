/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.glove;

import java.nio.file.Path;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.RetrieveVectorException;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorBasedWordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorSqliteDatabase;

/**
 * This word similarity measures utilizes GloVe trained word vector representations to calculate word similarity. It
 * retrieves vectors for each word and compares them using cosine similarity. This measure additionally manages a cache
 * to improve lookup speeds.
 */
public class GloveMeasure extends VectorBasedWordSimMeasure {

    private static final Logger LOGGER = LoggerFactory.getLogger(GloveMeasure.class);

    private final double similarityThreshold;

    /**
     * Constructs a new {@link GloveMeasure} using the settings provided by {@link CommonTextToolsConfig}.
     *
     * @throws SQLException if establishing the connection to the data source fails
     */
    public GloveMeasure() throws SQLException {
        this(new VectorSqliteDatabase(Path.of(CommonTextToolsConfig.GLOVE_DB_FILE_PATH)), CommonTextToolsConfig.GLOVE_SIMILARITY_THRESHOLD);
    }

    /**
     * Constructs a new {@link GloveMeasure} instance.
     *
     * @param dataSource          the data source from which word vectors are loaded
     * @param similarityThreshold the threshold above which words are considered similar, between 0 and 1
     * @throws IllegalArgumentException if the given threshold is not between 0 and 1
     */
    public GloveMeasure(VectorSqliteDatabase dataSource, double similarityThreshold) throws IllegalArgumentException {
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
            similarity = this.compareVectors(ctx.firstTerm(), ctx.secondTerm());
        } catch (RetrieveVectorException e) {
            LOGGER.error("Failed to compare glove vectors: " + ctx, e);
        }

        return similarity >= this.similarityThreshold;
    }

}
