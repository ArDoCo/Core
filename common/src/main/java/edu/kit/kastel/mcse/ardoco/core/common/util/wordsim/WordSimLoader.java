/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.common.util.wordsim;

import java.nio.file.Path;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.equality.EqualityMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText.DL4JFastTextDataSource;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText.FastTextMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.glove.GloveMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler.JaroWinklerMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.levenshtein.LevenshteinMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.nasari.NasariMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.ngram.NgramMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim.SEWordSimMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.wordnet.Ezzikouri;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.wordnet.WordNetMeasure;
import edu.mit.jwi.IRAMDictionary;
import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.ILoadPolicy;
import edu.uniba.di.lacam.kdde.lexical_db.ILexicalDatabase;
import edu.uniba.di.lacam.kdde.lexical_db.MITWordNet;
import edu.uniba.di.lacam.kdde.ws4j.RelatednessCalculator;
import edu.uniba.di.lacam.kdde.ws4j.similarity.JiangConrath;
import edu.uniba.di.lacam.kdde.ws4j.similarity.LeacockChodorow;
import edu.uniba.di.lacam.kdde.ws4j.similarity.Lesk;
import edu.uniba.di.lacam.kdde.ws4j.similarity.WuPalmer;
import edu.uniba.di.lacam.kdde.ws4j.util.WS4JConfiguration;

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
    public static List<WordSimMeasure> loadUsingProperties() {
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

                var dataSource = new DL4JFastTextDataSource(modelPath);

                LOGGER.info("Successfully loaded DL4J fastText data source!");

                var measure = new FastTextMeasure(dataSource, CommonTextToolsConfig.FASTTEXT_SIMILARITY_THRESHOLD);
                list.add(measure);
            }

            if (CommonTextToolsConfig.WORDNET_ENABLED) {
                Path wordNetDirPath = Path.of(CommonTextToolsConfig.WORDNET_DATA_DIR_PATH);
                IRAMDictionary dictionary = new RAMDictionary(wordNetDirPath.toFile(), ILoadPolicy.BACKGROUND_LOAD);
                dictionary.open();

                if (CommonTextToolsConfig.WORDNET_USE_CACHE) {
                    WS4JConfiguration.getInstance().setCache(true);
                }

                WS4JConfiguration.getInstance().setStem(true);

                ILexicalDatabase database = new MITWordNet(dictionary);
                Map<RelatednessCalculator, Double> calculatorThresholdMap = new HashMap<>();

                if (CommonTextToolsConfig.WORDNET_ALGO_LEACOCK_CHODOROW_ENABLED) {
                    calculatorThresholdMap.put(new LeacockChodorow(database), CommonTextToolsConfig.WORDNET_ALGO_LEACOCK_CHODOROW_THRESHOLD);
                }

                if (CommonTextToolsConfig.WORDNET_ALGO_WU_PALMER_ENABLED) {
                    calculatorThresholdMap.put(new WuPalmer(database), CommonTextToolsConfig.WORDNET_ALGO_WU_PALMER_THRESHOLD);
                }

                if (CommonTextToolsConfig.WORDNET_ALGO_JIANG_CONRATH_ENABLED) {
                    calculatorThresholdMap.put(new JiangConrath(database), CommonTextToolsConfig.WORDNET_ALGO_JIANG_CONRATH_THRESHOLD);
                }

                if (CommonTextToolsConfig.WORDNET_ALGO_EXTENDED_LESK_ENABLED) {
                    calculatorThresholdMap.put(new Lesk(database), CommonTextToolsConfig.WORDNET_ALGO_EXTENDED_LESK_THRESHOLD);
                }

                if (CommonTextToolsConfig.WORDNET_ALGO_EZZIKOURI_ENABLED) {
                    calculatorThresholdMap.put(new Ezzikouri(database), CommonTextToolsConfig.WORDNET_ALGO_EZZIKOURI_THRESHOLD);
                }

                var measure = new WordNetMeasure(calculatorThresholdMap);
                list.add(measure);
            }

            if (CommonTextToolsConfig.GLOVE_ENABLED) {
                list.add(new GloveMeasure());
            }

            if (CommonTextToolsConfig.NASARI_ENABLED) {
                list.add(new NasariMeasure());
            }

            return list;
        } catch (Exception e) {
            LOGGER.error("Failed to load word similarity measures", e);
            return Collections.emptyList();
        }
    }

    private WordSimLoader() {
    }

}
