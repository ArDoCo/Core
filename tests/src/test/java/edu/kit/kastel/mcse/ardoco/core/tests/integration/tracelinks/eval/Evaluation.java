/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval;

import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText.DL4JFastTextDataSource;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.fastText.FastTextMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.jarowinkler.JaroWinklerMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.ngram.NgramMeasure;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.measures.sewordsim.SEWordSimDataSource;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Evaluation {

    @Test
    @Disabled
    public void run() throws IOException, SQLException {
        var plans = getPlans();
        var resultDir = Path.of("eval_results");
        var evaluator = new Evaluator(plans, resultDir);

        evaluator.execute();
    }

    public static List<EvalPlan> getPlans() throws IOException, SQLException {
        var plans = new ArrayList<EvalPlan>();

        boolean jaroWinkler = false;
        boolean levenshtein = false;
        boolean ngram = false;
        boolean sewordsim = false;
        boolean fastText = false;
        boolean wordNetWP = false, wordNetLC = false, wordNetJC = false, wordNetLesk = false, wordNetEzzikouri = true;

        String fastTextModelName = "";
        DL4JFastTextDataSource fastTextDataSource = null;
        if (fastText) {
            var modelPath = Path.of(CommonTextToolsConfig.FASTTEXT_MODEL_FILE_PATH);
            fastTextModelName = modelPath.getFileName().toString().replace(".bin", "");
            fastTextDataSource = new DL4JFastTextDataSource(modelPath);
        }

        ILexicalDatabase wordNetDB = null;
        if (wordNetWP || wordNetLC || wordNetJC || wordNetLesk || wordNetEzzikouri) {
            WS4JConfiguration.getInstance().setCache(true);
            WS4JConfiguration.getInstance().setStem(true);

            Path wordNetDirPath = Path.of(CommonTextToolsConfig.WORDNET_DATA_DIR_PATH);
            IRAMDictionary dictionary = new RAMDictionary(wordNetDirPath.toFile(), ILoadPolicy.BACKGROUND_LOAD);
            dictionary.open();

            wordNetDB = new MITWordNet(dictionary);
        }

        for (int b = 1; b <= 2; b++) {
            for (int t = 0; t <= 100; t += 5) {
                double threshold = t / 100.0;

                // Jaro Winkler
                if (jaroWinkler && b == 1) {
                    plans.add(new EvalPlan("jaroWinkler", b, t, new JaroWinklerMeasure(threshold)));
                }

                // Levenshtein
                if (levenshtein) {
                    throw new UnsupportedOperationException();
                }

                // Ngram
                if (ngram) {
                    //plans.add(new EvalPlan("bigram", b, t, new NgramMeasure(NgramMeasure.Variant.LUCENE, 2, threshold)));
                    plans.add(new EvalPlan("trigram", b, t, new NgramMeasure(NgramMeasure.Variant.LUCENE, 3, threshold)));
                }

                // SEWordSim
                if (sewordsim) {
                    var dataSource = new SEWordSimDataSource(Path.of(CommonTextToolsConfig.SEWORDSIM_DB_FILE_PATH));
                    plans.add(new EvalPlan("sewsim", b, t, new SEWordSimMeasure(dataSource, threshold)));
                }

                // fastText
                if (fastText) {
                    plans.add(new EvalPlan("fastText_" + fastTextModelName, b, t, new FastTextMeasure(fastTextDataSource, threshold)));
                }

                // WordNet
                if (wordNetWP || wordNetLC || wordNetJC || wordNetLesk || wordNetEzzikouri) {
                    String group = "wordNet_";
                    var calcThresholdMap = new HashMap<RelatednessCalculator, Double>();

                    if (wordNetWP) {
                        group += "WP";
                        calcThresholdMap.put(new WuPalmer(wordNetDB), threshold);
                    }
                    if (wordNetLC) {
                        group += "LC";
                        calcThresholdMap.put(new LeacockChodorow(wordNetDB), threshold);
                    }
                    if (wordNetJC) {
                        group += "JC";
                        calcThresholdMap.put(new JiangConrath(wordNetDB), threshold);
                    }
                    if (wordNetLesk) {
                        group += "Lesk";
                        calcThresholdMap.put(new Lesk(wordNetDB), threshold);
                    }
                    if (wordNetEzzikouri) {
                        group += "Ezzi";
                        calcThresholdMap.put(new Ezzikouri(wordNetDB), threshold);
                    }

                    plans.add(new EvalPlan(group, b, t, new WordNetMeasure(calcThresholdMap)));
                }
            }
        }

        return plans;
    }

}
