/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

/**
 * This word similarity measures utilizes the SEWordSim database from Tian et al. 2014
 */
public class SEWordSimMeasure implements WordSimMeasure {

    private static final Logger LOGGER = LoggerFactory.getLogger(SEWordSimMeasure.class);

    private final SEWordSimDataSource dataSource;
    private final double similarityThreshold;

    /**
     * Constructs a new {@link SEWordSimMeasure} using the settings provided by {@link CommonTextToolsConfig}.
     *
     * @throws SQLException if establishing the connection to the data source fails
     */
    public SEWordSimMeasure() throws SQLException {
        this(new SEWordSimDataSource(Path.of(CommonTextToolsConfig.SEWORDSIM_DB_FILE_PATH)), CommonTextToolsConfig.SEWORDSIM_SIMILARITY_THRESHOLD);
    }

    /**
     * Constructs a new {@link SEWordSimMeasure} instance.
     *
     * @param dataSource          the data source from which word similarities are loaded
     * @param similarityThreshold the threshold above which words are considered similar, between 0 and 1
     */
    public SEWordSimMeasure(SEWordSimDataSource dataSource, double similarityThreshold) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.similarityThreshold = similarityThreshold;

        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("similarityThreshold outside of valid range: " + similarityThreshold);
        }
    }

    @Override
    public boolean areWordsSimilar(ComparisonContext ctx) {
        double similarity = Double.NaN;

        try {
            similarity = this.dataSource.getSimilarity(ctx.firstTerm(), ctx.secondTerm()).orElse(Double.NaN);
        } catch (SQLException e) {
            LOGGER.error("Failed to query the SEWordSim database for word comparison: " + ctx, e);
            return false;
        }

        if (Double.isNaN(similarity)) {
            return false; // words are probably missing from the database
        }

        return similarity >= this.similarityThreshold;
    }

}
