/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.ComparisonContext;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.deletelater.ComparisonStats;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.BabelNetDataSource;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.BabelNetSynsetId;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception.BabelNetInvalidKeyException;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.babelnet.exception.BabelNetRequestLimitException;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.RetrieveVectorException;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorBasedWordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.vector.VectorSqliteDatabase;

/**
 * This vector based word similarity measure utilizes Nasari trained word vector representations to calculate word
 * similarity. It retrieves vectors for each word and compares them using cosine similarity. This measure additionally
 * manages a cache to improve lookup speeds.
 */
public class NasariMeasure extends VectorBasedWordSimMeasure {

    private static final Logger LOGGER = LoggerFactory.getLogger(NasariMeasure.class);

    private final BabelNetDataSource babelNetData;
    private final double similarityThreshold;

    /**
     * Constructs a new {@link NasariMeasure} using the settings provided by {@link CommonTextToolsConfig}
     *
     * @throws IOException  if the babelNet data source cannot read from the cache
     * @throws SQLException if establishing the connection to the vector database fails
     */
    public NasariMeasure() throws IOException, SQLException {
        this(new BabelNetDataSource(CommonTextToolsConfig.BABELNET_API_KEY, Path.of(CommonTextToolsConfig.BABELNET_CACHE_FILE_PATH)),
                new VectorSqliteDatabase(Path.of(CommonTextToolsConfig.NASARI_DB_FILE_PATH)), CommonTextToolsConfig.NASARI_SIMILARITY_THRESHOLD);
    }

    /**
     * Constructs a new {@link NasariMeasure} instance.
     *
     * @param babelNetData        a data source that allows pulling data from babelNet
     * @param vectorDatabase      a vector database from which the pre-trained word vectors are loaded
     * @param similarityThreshold the threshold above which words are considered similar, between 0 and 1
     * @throws IllegalArgumentException if the given threshold is not between 0 and 1
     */
    public NasariMeasure(BabelNetDataSource babelNetData, VectorSqliteDatabase vectorDatabase, double similarityThreshold) throws IllegalArgumentException {
        super(vectorDatabase);
        this.babelNetData = Objects.requireNonNull(babelNetData);
        this.similarityThreshold = similarityThreshold;

        if (similarityThreshold < 0.0 || similarityThreshold > 1.0) {
            throw new IllegalArgumentException("similarityThreshold outside of valid range: " + similarityThreshold);
        }
    }

    @Override
    public boolean areWordsSimilar(ComparisonContext ctx) {
        List<BabelNetSynsetId> firstSenses;
        List<BabelNetSynsetId> secondSenses;

        try {
            firstSenses = this.babelNetData.getSensesOfLemma(ctx.firstTerm());
            secondSenses = this.babelNetData.getSensesOfLemma(ctx.secondTerm());
        } catch (IOException | InterruptedException | BabelNetInvalidKeyException | BabelNetRequestLimitException e) {
            LOGGER.error("Failed to get babelnet senses", e);
            return false;
        }

        for (BabelNetSynsetId firstSense : firstSenses) {
            for (BabelNetSynsetId secondSense : secondSenses) {
                double similarity;

                try {
                    similarity = compareVectors(firstSense.toString(), secondSense.toString());
                } catch (RetrieveVectorException e) {
                    LOGGER.error("Failed to compare nasari vectors", e);
                    return false;
                }

                if (similarity >= this.similarityThreshold) {
                    ComparisonStats.recordScore(similarity);
                    return true;
                }
            }
        }

        return false;
    }

}
