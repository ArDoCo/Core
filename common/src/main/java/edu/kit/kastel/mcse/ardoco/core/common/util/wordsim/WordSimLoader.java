/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.nio.file.Path;
import java.util.ArrayList;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.equality.EqualityMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fasttext.DL4JFastTextDataSource;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fasttext.FastTextMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler.JaroWinklerMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein.LevenshteinMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.ngram.NgramMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim.SEWordSimMeasure;

/**
 * Responsible for loading the word similarity measures that should be enabled according to the
 * {@link CommonTextToolsConfig}.
 */
public class WordSimLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(WordSimLoader.class);

    /**
     * Loads and returns the word similarity measures that should be enabled according to {@link CommonTextToolsConfig}.
     *
     * @return a list of word similarity measures
     */
    public static ImmutableList<WordSimMeasure> loadUsingProperties() {
        try {
            var list = new ArrayList<WordSimMeasure>();

            list.add(new EqualityMeasure());

            if (CommonTextToolsConfig.LEVENSHTEIN_ENABLED) {
                list.add(new LevenshteinMeasure());
            }

            if (CommonTextToolsConfig.JAROWINKLER_ENABLED) {
                list.add(new JaroWinklerMeasure());
            }

            if (CommonTextToolsConfig.NGRAM_ENABLED) {
                list.add(new NgramMeasure());
            }

            if (CommonTextToolsConfig.SEWORDSIM_ENABLED) {
                list.add(new SEWordSimMeasure());
            }

            if (CommonTextToolsConfig.FASTTEXT_ENABLED) {
                Path modelPath = Path.of(CommonTextToolsConfig.FASTTEXT_MODEL_FILE_PATH);

                LOGGER.info("Loading DL4J fastText data source...");

                try (var dataSource = new DL4JFastTextDataSource(modelPath)) {
                    LOGGER.info("Successfully loaded DL4J fastText data source!");

                    var measure = new FastTextMeasure(dataSource, CommonTextToolsConfig.FASTTEXT_SIMILARITY_THRESHOLD);
                    list.add(measure);
                }
            }

            return Lists.immutable.withAll(list);
        } catch (Exception e) {
            LOGGER.error("Failed to load word similarity measures", e);
            return Lists.immutable.empty();
        }
    }

    private WordSimLoader() {
    }

}
